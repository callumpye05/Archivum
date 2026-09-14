package com.cal.archivum.repository;

import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByWorld(World world);
    Optional<Location> findByIdAndWorldWorldIdAndWorldOwner(Long locationId , Long worldId , User currentUser);
    Optional<Location> findByIdAndWorldOwner(Long locationId , User Owner);
}
