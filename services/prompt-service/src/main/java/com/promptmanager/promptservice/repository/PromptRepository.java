package com.promptmanager.promptservice.repository;

import com.promptmanager.promptservice.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, UUID> {
	List<Prompt> findByUserId(String userId);

	List<Prompt> findByUserIdAndAiTool(String userId, String aiTool);

	List<Prompt> findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String userId, String title,
			String description);

	List<Prompt> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
