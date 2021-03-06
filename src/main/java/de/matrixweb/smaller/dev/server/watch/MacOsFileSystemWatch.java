package de.matrixweb.smaller.dev.server.watch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import com.barbarysoftware.watchservice.ClosedWatchServiceException;
import com.barbarysoftware.watchservice.StandardWatchEventKind;
import com.barbarysoftware.watchservice.WatchEvent;
import com.barbarysoftware.watchservice.WatchKey;
import com.barbarysoftware.watchservice.WatchService;
import com.barbarysoftware.watchservice.WatchableFile;

/**
 * @author marwol
 */
public class MacOsFileSystemWatch implements FileSystemWatch {

  private final WatchService watchService;

  private final Map<FileSystemWatchKey, Path> watches;

  /**
   * @param watches
   */
  public MacOsFileSystemWatch(final Map<FileSystemWatchKey, Path> watches) {
    this.watches = watches;
    this.watchService = WatchService.newWatchService();
  }

  /**
   * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch#register(java.nio.file.Path)
   */
  @Override
  public void register(final Path path) throws IOException {
    boolean watching = false;
    for (final Path current : this.watches.values()) {
      if (path.startsWith(current)) {
        watching = true;
      }
    }
    if (!watching) {
      this.watches.put(
          new MacOsWatchKey(new WatchableFile(path.toFile()).register(
              this.watchService, StandardWatchEventKind.OVERFLOW,
              StandardWatchEventKind.ENTRY_CREATE,
              StandardWatchEventKind.ENTRY_MODIFY,
              StandardWatchEventKind.ENTRY_DELETE)), path);
    }
  }

  /**
   * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch#take()
   */
  @Override
  public FileSystemWatchKey take() throws InterruptedException {
    try {
      return new MacOsWatchKey(this.watchService.take());
    } catch (final ClosedWatchServiceException e) {
      throw new FileSystemClosedWatchServiceException();
    }
  }

  /**
   * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch#close()
   */
  @Override
  public void close() throws IOException {
    this.watchService.close();
  }

  /** */
  public static class MacOsWatchKey implements FileSystemWatchKey {

    private final WatchKey watchKey;

    /**
     * @param watchKey
     */
    public MacOsWatchKey(final WatchKey watchKey) {
      this.watchKey = watchKey;
    }

    /**
     * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSystemWatchKey#pollEvents()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<FileSytemWatchEvent<?>> pollEvents() {
      return CollectionUtils.collect(this.watchKey.pollEvents(),
          new Transformer() {
            @Override
            public Object transform(final Object input) {
              return new MacOsFileSytemWatchEvent<>((WatchEvent<?>) input);
            }
          });
    }

    /**
     * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSystemWatchKey#reset()
     */
    @Override
    public boolean reset() {
      return this.watchKey.reset();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + (this.watchKey == null ? 0 : this.watchKey.hashCode());
      return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MacOsWatchKey other = (MacOsWatchKey) obj;
      if (this.watchKey == null) {
        if (other.watchKey != null) {
          return false;
        }
      } else if (!this.watchKey.equals(other.watchKey)) {
        return false;
      }
      return true;
    }

  }

  /**
   * @param <T>
   */
  public static class MacOsFileSytemWatchEvent<T> implements
      FileSytemWatchEvent<T> {

    private final WatchEvent<T> watchEvent;

    /**
     * @param watchEvent
     */
    public MacOsFileSytemWatchEvent(final WatchEvent<T> watchEvent) {
      this.watchEvent = watchEvent;
    }

    /**
     * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSytemWatchEvent#kind()
     */
    @Override
    public FileSytemWatchEvent.Kind<T> kind() {
      return new MacOsKind<T>(this.watchEvent.kind());
    }

    /**
     * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSytemWatchEvent#context()
     */
    @Override
    @SuppressWarnings("unchecked")
    public T context() {
      final Path path = ((File) this.watchEvent.context()).toPath();
      return (T) path;
    }

    /**
     * @param <T>
     */
    public static class MacOsKind<T> implements FileSytemWatchEvent.Kind<T> {

      private final WatchEvent.Kind<T> kind;

      /**
       * @param kind
       */
      public MacOsKind(
          final com.barbarysoftware.watchservice.WatchEvent.Kind<T> kind) {
        this.kind = kind;
      }

      /**
       * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSytemWatchEvent.Kind#isOverflow()
       */
      @Override
      public boolean isOverflow() {
        return this.kind == StandardWatchEventKind.OVERFLOW;
      }

      /**
       * @see de.matrixweb.smaller.dev.server.watch.FileSystemWatch.FileSytemWatchEvent.Kind#isEntryCreate()
       */
      @Override
      public boolean isEntryCreate() {
        return this.kind == StandardWatchEventKind.ENTRY_CREATE;
      }

    }

  }

}
