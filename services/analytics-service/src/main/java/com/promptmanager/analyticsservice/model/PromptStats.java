package com.promptmanager.analyticsservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "prompt_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"prompt_id", "user_id"})
)
public class PromptStats {

    @Id
    private UUID id;

    @Column(name = "prompt_id", nullable = false)
    private UUID promptId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private int views = 0;
    private int copies = 0;
    private int favorites = 0;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (id == null) id = UUID.randomUUID();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPromptId() { return promptId; }
    public void setPromptId(UUID promptId) { this.promptId = promptId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }

    public int getFavorites() { return favorites; }
    public void setFavorites(int favorites) { this.favorites = favorites; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
