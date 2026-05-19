package com.promptmanager.analyticsservice.repository;

import com.promptmanager.analyticsservice.dto.PromptSummaryDTO;
import com.promptmanager.analyticsservice.model.PromptStats;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PromptStatsRepository extends JpaRepository<PromptStats, UUID> {

	PromptStats findByPromptId(UUID promptId);

	boolean existsByPromptId(UUID promptId);

	@Modifying
	@Transactional
	@Query("DELETE FROM PromptStats ps WHERE ps.promptId = :promptId")
	void deleteByPromptIdSafe(@Param("promptId") UUID promptId);

	PromptStats findByPromptIdAndUserId(UUID promptId, String userId);

	@Query("""
			    SELECT new com.promptmanager.analyticsservice.dto.PromptSummaryDTO(
			        p.promptId,
			        SUM(p.favorites),
			        SUM(p.views),
			        SUM(p.copies)
			    )
			    FROM PromptStats p
			    WHERE p.views > 0
			    GROUP BY p.promptId
			    ORDER BY SUM(p.views) DESC
			""")
	List<PromptSummaryDTO> findMostViewed();

	//Most favorited
	@Query("""
			    SELECT new com.promptmanager.analyticsservice.dto.PromptSummaryDTO(
			        p.promptId,
			        SUM(p.favorites),
			        SUM(p.views),
			        SUM(p.copies)
			    )
			    FROM PromptStats p
			    WHERE p.favorites > 0
			    GROUP BY p.promptId
			    ORDER BY SUM(p.favorites) DESC
			""")
	List<PromptSummaryDTO> findMostFavorited();

	//Most copied
	@Query("""
			    SELECT new com.promptmanager.analyticsservice.dto.PromptSummaryDTO(
			        p.promptId,
			        SUM(p.favorites),
			        SUM(p.views),
			        SUM(p.copies)
			    )
			    FROM PromptStats p
			    WHERE p.copies > 0
			    GROUP BY p.promptId
			    ORDER BY SUM(p.copies) DESC
			""")
	List<PromptSummaryDTO> findMostCopied();

	@Query("SELECT ps FROM PromptStats ps WHERE ps.userId = :userId AND ps.views > 0 ORDER BY ps.views DESC")
	List<PromptStats> findMostViewedByUser(@Param("userId") String userId);

	@Query("SELECT ps FROM PromptStats ps WHERE ps.userId = :userId AND ps.copies > 0 ORDER BY ps.copies DESC")
	List<PromptStats> findMostCopiedByUser(@Param("userId") String userId);

	@Query("SELECT ps FROM PromptStats ps WHERE ps.userId = :userId AND ps.favorites > 0 ORDER BY ps.favorites DESC")
	List<PromptStats> findMostFavoriteByUser(@Param("userId") String userId);

	List<PromptStats> findAllByUserIdAndFavoritesGreaterThan(String userId, int favorites);

	List<PromptStats> findAllByPromptId(UUID promptId);
}
