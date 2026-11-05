package com.cal.archivum.service;

import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.dto.impl.WorldResponseDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;

import java.util.List;

public interface IWorldService {


    List<WorldResponseDto> getAllWorlds();
    WorldResponseDto getWorld(Long id);
    WorldResponseDto createWorld(CreateWorldDto dto);
    WorldResponseDto updateWorld(Long id, UpdateWorldDto dto);
    void deleteWorld(Long id);
    World transformFromDto(WorldDto dto);
    WorldResponseDto toDto(World world);
}
