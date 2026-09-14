package com.cal.archivum.repository;

import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldRepository extends JpaRepository<World,Long> {
    Optional<World> findByWorldIdAndOwner(Long id , User owner);
    List<World> findAllByOwner(User owner);

}
