package org.example.buttonforload.repository;

import lombok.AllArgsConstructor;
import org.example.buttonforload.dto.ResourceRowDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
@AllArgsConstructor
public class ResourceRowRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public int[] batchInsert(List<ResourceRowDto> rows) {
        String sql = """
                Insert into resource_row (
                    number,
                    full_name,
                    inclusion_grounds,
                    inclusion_decision_date,
                    exclusion_decision_date
                ) values (
                    :number,
                    :fullName,
                    :inclusionGrounds,
                    :inclusionDecisionDate,
                    :exclusionDecisionDate
                )
                """;
        MapSqlParameterSource[] batch = rows.stream()
                .map(row -> new MapSqlParameterSource()
                        .addValue("number", row.getNumber())
                        .addValue("fullName", row.getFullName())
                        .addValue("inclusionGrounds", row.getInclusionGrounds())
                        .addValue("inclusionDecisionDate",
                                row.getInclusionDecisionDate() != null ? Date.valueOf(row.getInclusionDecisionDate()) : null)
                        .addValue("exclusionDecisionDate",
                                row.getExclusionDecisionDate() != null ? Date.valueOf(row.getExclusionDecisionDate()) : null))
                .toArray(MapSqlParameterSource[]::new);

        return jdbcTemplate.batchUpdate(sql, batch);
    }
}
