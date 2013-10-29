package de.matrixweb.smaller.dev.server.templates;

import java.io.IOException;

import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * @author markusw
 */
public class NullTemplates implements TemplateEngine {

  /**
   * @see de.matrixweb.smaller.dev.server.templates.TemplateEngine#setVfs(de.matrixweb.smaller.resource.vfs.VFS)
   */
  @Override
  public void setVfs(final VFS vfs) {
  }

  /**
   * @see de.matrixweb.smaller.dev.server.templates.TemplateEngine#render(java.lang.String)
   */
  @Override
  public String render(final String path) throws IOException {
    throw new IOException("NullTemplate at " + path);
  }

}
