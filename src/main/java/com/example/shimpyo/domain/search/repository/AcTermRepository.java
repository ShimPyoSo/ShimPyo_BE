package com.example.shimpyo.domain.search.repository;

import com.example.shimpyo.domain.search.entity.AcTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AcTermRepository{

    private final JdbcTemplate jdbc;

    public AcTermRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Row> findByNormPrefix(String normPrefix, int limit, String type) {
        String sql = """
            SELECT term, type, ref_id, weight
              FROM ac_term
             WHERE type = ?
               AND term_norm LIKE CONCAT(?, '%')
             ORDER BY weight DESC
             LIMIT ?
        """;
        return jdbc.query(sql, (rs, i) -> new Row(
                rs.getString("term"),
                rs.getString("type"),
                rs.getString("ref_id"),
                rs.getInt("weight")
        ), type, normPrefix, limit);
    }

    public List<Row> findByChoseongPrefix(String choseongPrefix, int limit, String type) {
        String sql = """
            SELECT term, type, ref_id, weight
              FROM ac_term
             WHERE type = ?
               AND term_choseong LIKE CONCAT(?, '%')
             ORDER BY weight DESC
             LIMIT ?
        """;
        return jdbc.query(sql, (rs, i) -> new Row(
                rs.getString("term"),
                rs.getString("type"),
                rs.getString("ref_id"),
                rs.getInt("weight")
        ), type, choseongPrefix, limit);
    }

    public record Row(String term, String type, String refId, int weight) {}
}
