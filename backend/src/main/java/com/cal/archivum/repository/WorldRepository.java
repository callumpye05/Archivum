package com.cal.archivum.repository;

import com.cal.archivum.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepository extends JpaRepository<World,Long> {

}
