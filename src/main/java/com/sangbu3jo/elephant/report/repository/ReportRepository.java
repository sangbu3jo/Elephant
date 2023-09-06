package com.sangbu3jo.elephant.report.repository;


import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
