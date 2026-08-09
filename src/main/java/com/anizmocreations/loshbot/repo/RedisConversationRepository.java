package com.anizmocreations.loshbot.repo;

import com.anizmocreations.loshbot.config.ConditionalOnCacheProvider;
import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
@ConditionalOnCacheProvider("REDIS")
public class RedisConversationRepository implements ConversationRepository {

    private static final String CONV_KEY_PREFIX = "chat:conv:";
    private static final String MSG_KEY_PREFIX = "chat:msgs:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisConversationRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Conversation create(UUID userId, String title) {
        UUID id = UUID.randomUUID();
        Conversation conversation = new Conversation(id, userId, title, Instant.now());
        redisTemplate.opsForValue().set(CONV_KEY_PREFIX + id, conversation);

        // Index by user
        redisTemplate.opsForList().rightPush("chat:user_convs:" + userId, id.toString());

        return conversation;
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        Conversation conv = (Conversation) redisTemplate.opsForValue().get(CONV_KEY_PREFIX + id);
        return Optional.ofNullable(conv);
    }

    @Override
    public List<Conversation> findByUserId(UUID userId) {
        List<Object> ids = redisTemplate.opsForList().range("chat:user_convs:" + userId, 0, -1);
        if (ids == null) return Collections.emptyList();

        List<Conversation> convs = new ArrayList<>();
        for (Object id : ids) {
            findById(UUID.fromString((String) id)).ifPresent(convs::add);
        }
        return convs;
    }

    @Override
    public void saveMessage(Message message) {
        redisTemplate.opsForList().rightPush(MSG_KEY_PREFIX + message.getConversationId(), message);
    }

    @Override
    public List<Message> findMessages(UUID conversationId, int limit) {
        List<Object> msgs = redisTemplate.opsForList().range(MSG_KEY_PREFIX + conversationId, -limit, -1);
        if (msgs == null) return Collections.emptyList();

        List<Message> result = new ArrayList<>();
        for (Object m : msgs) {
            result.add((Message) m);
        }
        return result;
    }
}
