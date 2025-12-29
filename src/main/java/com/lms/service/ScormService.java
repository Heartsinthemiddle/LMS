package com.lms.service;

import com.lms.dto.response.ScormManifest;
import com.lms.entity.ScormCourse;
import com.lms.repository.ScormCourseREpository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ScormService {

    private static final Path ROOT = Paths.get("scorm-content");
    private final ScormCourseREpository courseRepository;

    public ScormService(ScormCourseREpository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public ScormCourse uploadAndRegister(MultipartFile file) throws ParserConfigurationException, IOException, SAXException {

        String courseId = UUID.randomUUID().toString();
        Path courseDir = ROOT.resolve(courseId);

        unzip(file, courseDir);
        ScormManifest manifest = parseManifest(courseDir);

        ScormCourse course = new ScormCourse();
        course.setTitle(manifest.getTitle());
        course.setVersion(manifest.getVersion());
        course.setLaunchUrl(manifest.getLaunchUrl());
        course.setBasePath(courseDir.toString());
        course.setCreatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    private void unzip(MultipartFile file, Path targetDir) {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName());
                Files.createDirectories(newPath.getParent());
                Files.copy(zis, newPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path findManifest(Path root) throws IOException {
        return Files.walk(root)
                .filter(p -> p.getFileName().toString().equalsIgnoreCase("imsmanifest.xml"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("imsmanifest.xml not found"));
    }

    private ScormManifest parseManifest(Path courseDir) throws IOException {

        Path manifestPath = findManifest(courseDir);

        if (!Files.exists(manifestPath)) {
            throw new RuntimeException("imsmanifest.xml not found in SCORM package");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document doc = factory.newDocumentBuilder()
                    .parse(manifestPath.toFile());

            // Title
            String title = "SCORM Course";
            NodeList titleNodes = doc.getElementsByTagNameNS("*", "title");
            if (titleNodes.getLength() > 0) {
                title = titleNodes.item(0).getTextContent();
            }

            // SCORM version
            String version = "SCORM_1_2";
            NodeList schemaNodes = doc.getElementsByTagNameNS("*", "schemaversion");
            if (schemaNodes.getLength() > 0) {
                String schema = schemaNodes.item(0).getTextContent();
                if (schema.contains("2004")) {
                    version = "SCORM_2004";
                }
            }

            // Launch file (SCO)
            NodeList resourceNodes = doc.getElementsByTagNameNS("*", "resource");
            if (resourceNodes.getLength() == 0) {
                throw new RuntimeException("No <resource> found in imsmanifest.xml");
            }

            Element resource = (Element) resourceNodes.item(0);
            String launchUrl = resource.getAttribute("href");

            if (launchUrl == null || launchUrl.isEmpty()) {
                throw new RuntimeException("Launch URL not found in imsmanifest.xml");
            }

            return new ScormManifest(title, version, launchUrl);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse imsmanifest.xml", e);
        }
    }



}
