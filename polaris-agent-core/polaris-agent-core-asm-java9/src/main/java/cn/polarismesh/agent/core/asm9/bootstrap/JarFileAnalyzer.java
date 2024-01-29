package cn.polarismesh.agent.core.asm9.bootstrap;

import cn.polarismesh.agent.core.asm9.module.Providers;
import cn.polarismesh.agent.core.common.utils.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileAnalyzer {

    private static final String META_INF = "META-INF/";

    private static final String CLASS_EXTENSION = ".class";

    private static final String SERVICE_LOADER = META_INF + "services/";

    private final JarFile jarFile;
    private final JarEntryFilter filter;
    private final ServiceLoaderEntryFilter serviceLoaderEntryFilter;


    public JarFileAnalyzer(JarFile jarFile) {
        this.jarFile = Objects.requireNonNull(jarFile, "jarFile");
        this.filter = new PackageFilter();
        this.serviceLoaderEntryFilter = new DefaultServiceLoaderEntryFilter();
    }

    public PackageInfo analyze() {
        Set<String> packageSet = new HashSet<>();
        List<Providers> providesList = new ArrayList<>();

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();

            final String packageName = this.filter.filter(jarEntry);
            if (packageName != null) {
                packageSet.add(packageName);
            }

            final Providers provides = this.serviceLoaderEntryFilter.filter(jarEntry);
            if (provides != null) {
                providesList.add(provides);
            }
        }
        return new PackageInfo(packageSet, providesList);
    }


    interface ServiceLoaderEntryFilter {
        Providers filter(JarEntry jarEntry);
    }

    class DefaultServiceLoaderEntryFilter implements ServiceLoaderEntryFilter {
        @Override
        public Providers filter(JarEntry jarEntry) {
            final String jarEntryName = jarEntry.getName();
            if (!jarEntryName.startsWith(SERVICE_LOADER)) {
                return null;
            }
            if (jarEntry.isDirectory()) {
                return null;
            }
            if (jarEntryName.indexOf('/', SERVICE_LOADER.length()) != -1) {
                return null;
            }
            try {
                InputStream inputStream = jarFile.getInputStream(jarEntry);

                ServiceDescriptorParser parser = new ServiceDescriptorParser();
                List<String> serviceImplClassName = parser.parse(inputStream);
                String serviceClassName = jarEntryName.substring(SERVICE_LOADER.length());
                return new Providers(serviceClassName, serviceImplClassName);
            } catch (IOException e) {
                throw new IllegalStateException(jarFile.getName() + " File read fail ", e);
            }
        }

    }


    interface JarEntryFilter {
        String filter(JarEntry jarEntry);
    }

    static class PackageFilter implements JarEntryFilter {
        public String filter(JarEntry jarEntry) {
            if (jarEntry.getName().startsWith(META_INF)) {
                // skip META-INF
                return null;
            }
            if (jarEntry.isDirectory()) {
                // skip empty dir
                return null;
            }

            final String fileName = jarEntry.getName();
            if (!checkFIleExtension(fileName, CLASS_EXTENSION)) {
                // skip non class file
                return null;
            }

            final String packageName = ClassUtils.getPackageName(fileName, '/', null);
            if (packageName == null) {
                return null;
            }
            return toPackageName(packageName);
        }

        private boolean checkFIleExtension(String fileName, String extension) {
            return fileName.endsWith(extension);
        }


        private String toPackageName(String dirFormat) {
            if (dirFormat == null) {
                return null;
            }
            return dirFormat.replace('/', '.');
        }
    }
}
