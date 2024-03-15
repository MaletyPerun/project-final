package com.javarush.jira.bugtracking.task;

import com.javarush.jira.bugtracking.Handlers;
import com.javarush.jira.bugtracking.task.to.ActivityTo;
import com.javarush.jira.common.error.DataConflictException;
import com.javarush.jira.login.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.javarush.jira.bugtracking.task.TaskUtil.getLatestValue;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final TaskRepository taskRepository;

    private final Handlers.ActivityHandler handler;

    private static void checkBelong(HasAuthorId activity) {
        if (activity.getAuthorId() != AuthUser.authId()) {
            throw new DataConflictException("Activity " + activity.getId() + " doesn't belong to " + AuthUser.get());
        }
    }

    public Duration timeInWork(long taskId) {
        List<Activity> activities = getActivities(taskId);

        boolean isStartProccess = false;
        long seconds = 0;
        LocalDateTime lastTime = null;

        for (Activity activity : activities) {
            System.out.println(activity.getStatusCode());
            if (activity.getStatusCode().equals("in_progress")) {
                lastTime = activity.getUpdated();
                if (lastTime == null) {
                    lastTime = LocalDateTime.now();
                }
                isStartProccess = true;
            } else if (isStartProccess) {
                if (activity.getStatusCode().equals("ready_for_review")) {
                    seconds += Duration.between(lastTime, activity.getUpdated()).getSeconds();
                    isStartProccess = false;
                } else if (activity.getStatusCode().equals("canceled")) {
                    seconds += Duration.between(lastTime, activity.getUpdated()).getSeconds();
                    isStartProccess = false;
                    break;
                }
            }
        }
        if (lastTime != null && isStartProccess) {
            seconds += Duration.between(lastTime, LocalDateTime.now()).getSeconds();
        }

        printTime(Duration.ofSeconds(seconds));
        return Duration.ofSeconds(seconds);
    }

    public Duration timeInTest(long taskId) {
        List<Activity> activities = getActivities(taskId);

        boolean isStartProccess = false;
        long seconds = 0;
        LocalDateTime lastTime = null;

        for (Activity activity : activities) {
            System.out.println(activity.getStatusCode());
            if (activity.getStatusCode().equals("ready_for_review")) {
                lastTime = activity.getUpdated();
                if (lastTime == null) {
                    lastTime = LocalDateTime.now();
                }
                isStartProccess = true;
            } else if(isStartProccess) {
                if (activity.getStatusCode().equals("done") || activity.getStatusCode().equals("canceled")) {
                    seconds += Duration.between(lastTime, activity.getUpdated()).getSeconds();
                    isStartProccess = false;
                    break;
                } else if (activity.getStatusCode().equals("in_progress")) {
                    seconds += Duration.between(lastTime, activity.getUpdated()).getSeconds();
                    isStartProccess = false;
                }
            }
        }
        if (lastTime != null && isStartProccess) {
            seconds += Duration.between(lastTime, LocalDateTime.now()).getSeconds();
        }
        printTime(Duration.ofSeconds(seconds));
        return Duration.ofSeconds(seconds);
    }

    private List<Activity> getActivities(long taskId) {
        Task task = taskRepository.getExisted(taskId);
        return handler.getRepository().findAllByTaskIdOrderByUpdatedAsc(task.id());
    }

    private void printTime(Duration secondsInProcess) {
        System.out.printf("Time in work: %s days %s hours %s minutes\n",
                secondsInProcess.toDays(),
                secondsInProcess.toHours(),
                secondsInProcess.toMinutes());
    }

    @Transactional
    public Activity create(ActivityTo activityTo) {
        checkBelong(activityTo);
        Task task = taskRepository.getExisted(activityTo.getTaskId());
        if (activityTo.getStatusCode() != null) {
            task.checkAndSetStatusCode(activityTo.getStatusCode());
        }
        if (activityTo.getTypeCode() != null) {
            task.setTypeCode(activityTo.getTypeCode());
        }
        return handler.createFromTo(activityTo);
    }

    @Transactional
    public void update(ActivityTo activityTo, long id) {
        checkBelong(handler.getRepository().getExisted(activityTo.getId()));
        handler.updateFromTo(activityTo, id);
        System.out.println(timeInWork(id));
        System.out.println(timeInTest(id));
        updateTaskIfRequired(activityTo.getTaskId(), activityTo.getStatusCode(), activityTo.getTypeCode());
    }

    @Transactional
    public void delete(long id) {
        Activity activity = handler.getRepository().getExisted(id);
        checkBelong(activity);
        handler.delete(activity.id());
        updateTaskIfRequired(activity.getTaskId(), activity.getStatusCode(), activity.getTypeCode());
    }

    private void updateTaskIfRequired(long taskId, String activityStatus, String activityType) {
        if (activityStatus != null || activityType != null) {
            timeInWork(taskId);
            timeInTest(taskId);
            Task task = taskRepository.getExisted(taskId);
            List<Activity> activities = handler.getRepository().findAllByTaskIdOrderByUpdatedDesc(task.id());
            if (activityStatus != null) {
                String latestStatus = getLatestValue(activities, Activity::getStatusCode);
                if (latestStatus == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setStatusCode(latestStatus);
            }
            if (activityType != null) {
                String latestType = getLatestValue(activities, Activity::getTypeCode);
                if (latestType == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setTypeCode(latestType);
            }
        }
    }
}
