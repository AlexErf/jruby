package org.jruby.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnr.posix.FileStat;
import jnr.posix.POSIX;

import org.jruby.runtime.load.ExtendedFileResource;

abstract class JarResource implements ExtendedFileResource {
    private static Pattern PREFIX_MATCH = Pattern.compile("^(?:jar:)?(?:file:)?(.*)$");

    private static final JarCache jarCache = new JarCache();

    public static JarResource create(String pathname) {
        Matcher matcher = PREFIX_MATCH.matcher(pathname);
        String sanitized = matcher.matches() ? matcher.group(1) : pathname;

        int bang = sanitized.indexOf('!');
        if (bang < 0) {
            return null;
        }

        String jarPath;
        String entryPath;
        try
        {
            // since pathname is actually an uri we need to decode any url decoded characters like %20
            // which happens when directory names contain spaces
            jarPath = URLDecoder.decode(sanitized.substring(0, bang), "UTF-8");
            entryPath = URLDecoder.decode(sanitized.substring(bang + 1), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException( "hmm - system does not know UTF-8 string encoding :(" );
        }

        // TODO: Do we really need to support both test.jar!foo/bar.rb and test.jar!/foo/bar.rb cases?
        JarResource resource = createJarResource(jarPath, entryPath, false);

        if (resource == null && entryPath.startsWith("/")) {
            resource = createJarResource(jarPath, entryPath.substring(1), true);
        }

        return resource;
    }

    private static JarResource createJarResource(String jarPath, String entryPath, boolean rootSlashPrefix) {
        JarCache.JarIndex index = jarCache.getIndex(jarPath);

        if (index == null) {
            // Jar doesn't exist
            return null;
        }

        // Try it as directory first, because jars tend to have foo/ entries
        // and it's not really possible disambiguate between files and directories.
        String[] entries = index.getDirEntries(entryPath);
        if (entries != null) {
            return new JarDirectoryResource(jarPath, rootSlashPrefix, entryPath, entries);
        }

        JarEntry jarEntry = index.getJarEntry(entryPath);
        if (jarEntry != null) {
            InputStream jarEntryStream = index.getInputStream(jarEntry);
            return new JarFileResource(jarPath, rootSlashPrefix, jarEntry, jarEntryStream);
        }

        return null;
    }

    private final String jarPrefix;
    private final JarFileStat fileStat;

    protected JarResource(String jarPath, boolean rootSlashPrefix) {
        this.jarPrefix = rootSlashPrefix ? jarPath + "!/" : jarPath + "!";
        this.fileStat = new JarFileStat(this);
    }

    @Override
    public String absolutePath() {
        return jarPrefix + entryName();
    }

    @Override
    public URL getURL()
    {
        try
        {
            return new URL( "jar:" + absolutePath() );
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    @Override
    public boolean exists() {
        // If a jar resource got created, then it always corresponds to some kind of resource
        return true;
    }

    @Override
    public boolean canRead() {
        // Can always read from a jar
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean isSymLink() {
        // Jar archives shouldn't contain symbolic links, or it would break portability. `jar`
        // command behavior seems to comform to that (it unwraps syumbolic links when creating a jar
        // and replaces symbolic links with regular file when extracting from a zip that contains
        // symbolic links). Also see:
        // http://www.linuxquestions.org/questions/linux-general-1/how-to-create-jar-files-with-symbolic-links-639381/
        return false;
    }

    @Override
    public FileStat stat(POSIX posix) {
        return fileStat;
    }

    @Override
    public FileStat lstat(POSIX posix) {
      // jars don't have symbolic links, so lstat is no different than regular stat
      return stat(posix);
    }

    @Override
    public JRubyFile hackyGetJRubyFile() {
      return JRubyNonExistentFile.NOT_EXIST;
    }

    abstract protected String entryName();
}
