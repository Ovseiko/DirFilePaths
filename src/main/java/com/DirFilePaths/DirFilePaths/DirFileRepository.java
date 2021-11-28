package com.DirFilePaths.DirFilePaths;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface DirFileRepository extends CrudRepository<DirFile, Integer> {
    List<DirFile> findByIdParent(Integer idParent);
    List<DirFile> findByName(String name);
    boolean existsByNameAndIdParent(String name, Integer idParent);
    List<DirFile> findByNameAndIdParent(String name, Integer idParent);

}
