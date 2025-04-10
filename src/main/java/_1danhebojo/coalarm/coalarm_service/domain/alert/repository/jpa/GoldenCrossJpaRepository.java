package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.GoldenCrossEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoldenCrossJpaRepository extends JpaRepository<GoldenCrossEntity, Long> {
    @Modifying
    @Query("DELETE FROM GoldenCrossEntity g WHERE g.alert.id IN :alertIds")
    void deleteByAlertIdIn(@Param("alertIds") List<Long> alertIds);
    // 골든 크로스 감지가 있는 경우 golden_cross 정보 포함 조회
    @Query("    SELECT t" +
            "    FROM GoldenCrossEntity t" +
            "    JOIN FETCH t.alert a" +
//            "    JOIN FETCH a.user u " +
            "    JOIN FETCH a.coin c" +
            "    WHERE a.isGoldenCross = true AND a.id = :alertId")
    Optional<GoldenCrossEntity> findGoldenCrossAlertsByAlertId(Long alertId);
}
