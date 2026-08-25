package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CreateNotificationRequestDTO;
import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.entity.Notification;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationType;
import com.cafestory.mapper.NotificationMapper;
import com.cafestory.repository.NotificationRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceInterface.NotificationRealtimeService;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.ActorContextResolver;
import com.cafestory.validation.UserValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;
    private final UserValidator userValidator;
    private final ActorContextResolver actorContextResolver;
    private final NotificationMapper notificationMapper;
    private final NotificationRealtimeService notificationRealtimeService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserValidator userValidator,
            ActorContextResolver actorContextResolver,
            NotificationMapper notificationMapper,
            NotificationRealtimeService notificationRealtimeService) {
        this.notificationRepository = notificationRepository;
        this.userValidator = userValidator;
        this.actorContextResolver = actorContextResolver;
        this.notificationMapper = notificationMapper;
        this.notificationRealtimeService = notificationRealtimeService;
    }

    @Override
    @Transactional
    public NotificationResponseDTO createNotification(CreateNotificationRequestDTO request) {
        validateCreateRequest(request);
        ActorContext actorContext = actorContextResolver.resolve(
                request.getActorId(),
                request.getActorContextType(),
                request.getActorCafePageId());
        if (request.getActorId().equals(request.getRecipientId())
                && actorContext.actorContextType() == ActorContextType.USER) {
            return null;
        }

        User recipient = userValidator.validateUserExists(request.getRecipientId());

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actorContext.actorUser());
        notification.setActorContextType(actorContext.actorContextType());
        notification.setActorCafePage(actorContext.actorCafePage());
        notification.setType(request.getType());
        notification.setBlogId(request.getBlogId());
        notification.setConversationId(request.getConversationId());
        notification.setTargetUserId(request.getUserId());
        notification.setTargetType(request.getTargetType());
        notification.setTargetCafePageId(request.getCafePageId());
        notification.setCommentId(request.getCommentId());
        notification.setMessageId(request.getMessageId());
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponseDTO response = notificationMapper.toNotificationResponseDTO(savedNotification);
        notificationRealtimeService.emitNewNotification(recipient.getUserId(), response);
        notificationRealtimeService.emitUnreadCountUpdated(recipient.getUserId(), getUnreadCount(recipient.getUserId()));
        return response;
    }

    @Override
    public NotificationResponseDTO createLikeNotification(UUID recipientId, UUID actorId, UUID blogId) {
        return createLikeNotification(recipientId, actorId, blogId, ActorContextType.USER, null);
    }

    @Override
    public NotificationResponseDTO createLikeNotification(
            UUID recipientId,
            UUID actorId,
            UUID blogId,
            ActorContextType actorContextType,
            UUID actorCafePageId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.LIKE);
        request.setBlogId(blogId);
        request.setActorContextType(actorContextType);
        request.setActorCafePageId(actorCafePageId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createShareNotification(UUID recipientId, UUID actorId, UUID blogId) {
        return createShareNotification(recipientId, actorId, blogId, ActorContextType.USER, null);
    }

    @Override
    public NotificationResponseDTO createShareNotification(
            UUID recipientId,
            UUID actorId,
            UUID blogId,
            ActorContextType actorContextType,
            UUID actorCafePageId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.SHARE);
        request.setBlogId(blogId);
        request.setActorContextType(actorContextType);
        request.setActorCafePageId(actorCafePageId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createCommentNotification(UUID recipientId, UUID actorId, UUID blogId, UUID commentId) {
        return createCommentNotification(recipientId, actorId, blogId, commentId, ActorContextType.USER, null);
    }

    @Override
    public NotificationResponseDTO createCommentNotification(
            UUID recipientId,
            UUID actorId,
            UUID blogId,
            UUID commentId,
            ActorContextType actorContextType,
            UUID actorCafePageId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.COMMENT);
        request.setBlogId(blogId);
        request.setCommentId(commentId);
        request.setActorContextType(actorContextType);
        request.setActorCafePageId(actorCafePageId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createMessageNotification(UUID recipientId, UUID actorId, UUID conversationId, UUID messageId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.MESSAGE);
        request.setConversationId(conversationId);
        request.setMessageId(messageId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createFollowNotification(UUID recipientId, UUID actorId, UUID userId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.FOLLOW);
        request.setTargetType(FollowTargetType.USER);
        request.setUserId(userId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createFollowPageNotification(UUID recipientId, UUID actorId, UUID cafePageId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.FOLLOW);
        request.setTargetType(FollowTargetType.CAFE_PAGE);
        request.setCafePageId(cafePageId);
        return createNotification(request);
    }

    @Override
    public NotificationResponseDTO createTagNotification(UUID recipientId, UUID actorId, UUID blogId) {
        CreateNotificationRequestDTO request = baseRequest(recipientId, actorId, NotificationType.TAG);
        request.setBlogId(blogId);
        return createNotification(request);
    }

    @Override
    @Transactional
    public NotificationResponseDTO createModerationNotification(UUID recipientId, UUID blogId, String moderationStatus, String moderationReason) {
        if (recipientId == null || blogId == null || moderationStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipientId, blogId and moderationStatus are required");
        }
        User recipient = userValidator.validateUserExists(recipientId);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(recipient);
        notification.setActorContextType(ActorContextType.USER);
        notification.setType(NotificationType.BLOG_MODERATION);
        notification.setBlogId(blogId);
        notification.setModerationStatus(moderationStatus);
        notification.setModerationReason(moderationReason);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponseDTO response = notificationMapper.toNotificationResponseDTO(savedNotification);
        notificationRealtimeService.emitNewNotification(recipient.getUserId(), response);
        notificationRealtimeService.emitUnreadCountUpdated(recipient.getUserId(), getUnreadCount(recipient.getUserId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(
            UUID userId,
            int page,
            int limit,
            Boolean isRead,
            NotificationType type) {
        userValidator.validateUserExists(userId);
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.max(1, Math.min(limit, MAX_LIMIT)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications;
        if (type != null && isRead != null) {
            notifications = notificationRepository.findByRecipientUserIdAndTypeAndIsRead(userId, type, isRead, pageRequest);
        } else if (type != null) {
            notifications = notificationRepository.findByRecipientUserIdAndType(userId, type, pageRequest);
        } else if (isRead != null) {
            notifications = notificationRepository.findByRecipientUserIdAndIsRead(userId, isRead, pageRequest);
        } else {
            notifications = notificationRepository.findByRecipientUserId(userId, pageRequest);
        }
        return notifications.map(notificationMapper::toNotificationResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        userValidator.validateUserExists(userId);
        return notificationRepository.countByRecipientUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(UUID userId, UUID notificationId) {
        Notification notification = validateOwnedNotification(userId, notificationId);
        notification.setIsRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        notificationRealtimeService.emitUnreadCountUpdated(userId, getUnreadCount(userId));
        return notificationMapper.toNotificationResponseDTO(savedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        userValidator.validateUserExists(userId);
        List<Notification> notifications = notificationRepository.findByRecipientUserIdAndIsRead(userId, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
        notificationRealtimeService.emitUnreadCountUpdated(userId, 0);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID userId, UUID notificationId) {
        Notification notification = validateOwnedNotification(userId, notificationId);
        notificationRepository.delete(notification);
        notificationRealtimeService.emitUnreadCountUpdated(userId, getUnreadCount(userId));
    }

    private Notification validateOwnedNotification(UUID userId, UUID notificationId) {
        userValidator.validateUserExists(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getRecipient().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access another user's notification");
        }
        return notification;
    }

    private void validateCreateRequest(CreateNotificationRequestDTO request) {
        if (request.getRecipientId() == null || request.getActorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Actor id and recipient id are required");
        }
        if (request.getType() == null || !List.of(NotificationType.LIKE, NotificationType.SHARE, NotificationType.COMMENT,
                NotificationType.MESSAGE, NotificationType.FOLLOW, NotificationType.TAG).contains(request.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification type");
        }
        switch (request.getType()) {
            case LIKE, SHARE, COMMENT, TAG -> validateBlogTarget(request);
            case MESSAGE -> validateMessageTarget(request);
            case FOLLOW -> validateFollowTarget(request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification type");
        }
    }

    private void validateBlogTarget(CreateNotificationRequestDTO request) {
        if (request.getBlogId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "blogId is required");
        }
        if (request.getConversationId() != null || request.getUserId() != null || request.getCafePageId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mismatched target data");
        }
    }

    private void validateMessageTarget(CreateNotificationRequestDTO request) {
        if (request.getConversationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId is required");
        }
        if (request.getBlogId() != null || request.getUserId() != null || request.getCafePageId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mismatched target data");
        }
    }

    private void validateFollowTarget(CreateNotificationRequestDTO request) {
        FollowTargetType targetType = request.getTargetType() == null ? FollowTargetType.USER : request.getTargetType();
        request.setTargetType(targetType);
        if (targetType == FollowTargetType.USER && request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (targetType == FollowTargetType.CAFE_PAGE && request.getCafePageId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cafePageId is required");
        }
        if (request.getBlogId() != null || request.getConversationId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mismatched target data");
        }
        if (targetType == FollowTargetType.USER && request.getCafePageId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mismatched target data");
        }
        if (targetType == FollowTargetType.CAFE_PAGE && request.getUserId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mismatched target data");
        }
    }

    private CreateNotificationRequestDTO baseRequest(UUID recipientId, UUID actorId, NotificationType type) {
        CreateNotificationRequestDTO request = new CreateNotificationRequestDTO();
        request.setRecipientId(recipientId);
        request.setActorId(actorId);
        request.setType(type);
        return request;
    }
}
