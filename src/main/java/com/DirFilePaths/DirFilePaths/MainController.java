package com.DirFilePaths.DirFilePaths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping(path="/")
public class MainController {
    @Autowired
    private DirFileRepository dirFileRepository;
    private final List<String> listPaths = new ArrayList<>();

    @GetMapping(path="/add")
    public @ResponseBody String addNewDirFile(@RequestParam(value = "path") String mainPath) {
        final File file = new File(mainPath);
        String answer = "Saved";
        if(file.exists()) writeAllPath(file, 0);
        else answer = "The file/directory doesn't exist or its existence cannot be verified.";
        return answer;
    }

    public @ResponseBody Integer getIdFindByNameIdParent(@RequestParam(value = "name") String name,
                                                         @RequestParam(value = "idP") Integer idP){

        return dirFileRepository.findByNameAndIdParent(name, idP).get(0).getId();
    }

    public @ResponseBody void saveDirFile(String nameDF, int idP){
        DirFile n = new DirFile();
        n.setName(nameDF);
        n.setIdParent(idP);
        dirFileRepository.save(n);
    }

    public @ResponseBody void writeAllPath(File file, int idP){
        if(idP == 0){
            Path mainPath = file.toPath();
            int countDir = mainPath.getNameCount();
            if(countDir > 1){
                for(int i = 0; i < countDir-1; i++){
                    String name = mainPath.getName(i).toString();
                    if(!dirFileRepository.existsByNameAndIdParent(name, idP)){
                        saveDirFile(name, idP);
                    }
                    idP = getIdFindByNameIdParent(name, idP);
                }
            }
        }
        String name = file.getName();
        if(!dirFileRepository.existsByNameAndIdParent(name, idP)){
            saveDirFile(name, idP);
        }
        idP = getIdFindByNameIdParent(name, idP);

        if(file.isDirectory()){
            for(File dir: Objects.requireNonNull(file.listFiles())){
                writeAllPath(dir, idP);
            }
        }
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<DirFile> getAllPaths() {
        return dirFileRepository.findAll();
    }

    public @ResponseBody Integer getIdPFindById(@RequestParam(value = "findById") Integer findById) {
        return dirFileRepository.findById(findById).get().getIdParent();
    }

    @GetMapping(path="/delAllPath")
    public @ResponseBody String DelPathAll() {
        dirFileRepository.deleteAll();
        return "Deleted";
    }

    @GetMapping(path="/findIdParent")
    public @ResponseBody List<DirFile> getFindByIdParent(@RequestParam(value = "idParent") Integer idParent) {
        return dirFileRepository.findByIdParent(idParent);
    }

    @GetMapping(path="/findName")
    public @ResponseBody List<DirFile> getFindByName(@RequestParam(value = "name") String name) {
        return dirFileRepository.findByName(name);
    }

    @GetMapping(path="/getPaths")
    public @ResponseBody List<String> getPaths(@RequestParam(value = "idP", defaultValue = "0",  required = false) Integer idP,
                                                             @RequestParam(value = "pathsDF", defaultValue = "C:\\",  required = false) String pathsDF,
                                                             @RequestParam(value = "name", defaultValue = "", required = false) String name) {
        listPaths.clear();
        return getListPaths(idP, pathsDF, name);
    }

    public @ResponseBody List<String> getListPaths(Integer idP, String pathsDF, String name) {
        List<DirFile> listDirFile;
        List<String> pathsList = new ArrayList<>();
        if(!name.isEmpty()){
            listDirFile = getFindByName(name);
            for(DirFile el: listDirFile){
                StringBuilder bufPath = new StringBuilder(pathsDF);
                idP = el.getIdParent();
                while(idP!=0){
                    String nameParent = dirFileRepository.findById(idP).get().getName() + "\\";
                    bufPath.insert(3, nameParent);
                    idP = getIdPFindById(idP);
                }
                pathsList.add(bufPath.toString());
            }
        } else listDirFile = getFindByIdParent(idP);
        if(!listDirFile.isEmpty()){
            int i=0;
            for(DirFile el : listDirFile){
                if(!pathsList.isEmpty()) {
                    pathsDF = pathsList.get(i);
                    i++;
                }
                String s = pathsDF + el.getName();
                listPaths.add(s);
                getListPaths(el.getId(), s + "\\", "");
            }
        }
        return listPaths;
    }
}
