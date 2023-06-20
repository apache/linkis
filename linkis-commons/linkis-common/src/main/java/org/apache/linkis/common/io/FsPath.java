/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.common.io;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class FsPath {

  public static final String CUR_DIR = ".";
  public static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");
  public static final char SEPARATOR_CHAR = '/';
  public static final String SEPARATOR = "/";
  private static final Pattern hasDriveLetterSpecifier = Pattern.compile("^/?[a-zA-Z]:");

  private URI uri;
  private long modification_time;
  private long access_time;
  private long length;
  private boolean isdir;
  private String permissionString;
  private String owner;
  private String group;
  private PosixFilePermission[] permissions;

  public FsPath(String pathString) throws IllegalArgumentException {
    checkPathArg(pathString);
    // We can't use 'new URI(String)' directly, since it assumes things are
    // escaped, which we don't require of Paths.

    // add a slash in front of paths with Windows drive letters
    if (hasWindowsDrive(pathString) && pathString.charAt(0) != '/') {
      pathString = "/" + pathString;
    }
    // parse uri components
    String scheme = null;
    String authority = null;

    int start = 0;
    // parse uri scheme, if any
    int colon = pathString.indexOf(':');
    int slash = pathString.indexOf('/');
    if ((colon != -1) && ((slash == -1) || (colon < slash))) { // has a scheme
      scheme = pathString.substring(0, colon);
      start = colon + 1;
    }
    // parse uri authority, if any
    if (pathString.startsWith("//", start) && (pathString.length() - start > 2)) { // has authority
      int nextSlash = pathString.indexOf('/', start + 2);
      int authEnd = nextSlash > 0 ? nextSlash : pathString.length();
      authority = pathString.substring(start + 2, authEnd);
      start = authEnd;
    }

    // uri path is the rest of the string -- query & fragment not supported
    String path = pathString.substring(start, pathString.length());
    initialize(scheme, authority, path, null);
  }

  public FsPath(String scheme, String authority, String path) {
    checkPathArg(path);
    // add a slash in front of paths with Windows drive letters
    if (hasWindowsDrive(path) && path.charAt(0) != '/') {
      path = "/" + path;
    }
    // add "./" in front of Linux relative paths so that a path containing
    // a colon e.q. "a:b" will not be interpreted as scheme "a".
    if (!WINDOWS && path.charAt(0) != '/') {
      path = "./" + path;
    }
    initialize(scheme, authority, path, null);
  }

  private void checkPathArg(String path) throws IllegalArgumentException {
    // disallow construction of a Path from an empty string
    if (path == null) {
      throw new IllegalArgumentException("Can not create a Path from a null string");
    }
    if (path.length() == 0) {
      throw new IllegalArgumentException("Can not create a Path from an empty string");
    }
  }

  private void initialize(String scheme, String authority, String path, String fragment) {
    try {
      this.uri =
          new URI(scheme, authority, normalizePath(scheme, path), null, fragment).normalize();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String normalizePath(String scheme, String path) {
    // Remove double forward slashes.
    path = StringUtils.replace(path, "//", "/");

    // Remove backslashes if this looks like a Windows path. Avoid
    // the substitution if it looks like a non-local URI.
    if (WINDOWS
        && (hasWindowsDrive(path)
            || (scheme == null)
            || (scheme.isEmpty())
            || (scheme.equals("file")))) {
      path = StringUtils.replace(path, "\\", "/");
    }

    // trim trailing slash from non-root path (ignoring windows drive)
    int minLength = startPositionWithoutWindowsDrive(path) + 1;
    if (path.length() > minLength && path.endsWith(SEPARATOR)) {
      path = path.substring(0, path.length() - 1);
    }

    return path;
  }

  private static boolean hasWindowsDrive(String path) {
    return (WINDOWS && hasDriveLetterSpecifier.matcher(path).find());
  }

  private static int startPositionWithoutWindowsDrive(String path) {
    if (hasWindowsDrive(path)) {
      return path.charAt(0) == SEPARATOR_CHAR ? 3 : 2;
    } else {
      return 0;
    }
  }

  /** @return if no storage type pointed, hdfs type will returned */
  public String getFsType() {
    return getFsType("file");
  }

  public String getFsType(String defaultType) {
    String scheme = uri.getScheme();
    if (StringUtils.isEmpty(scheme)) {
      return defaultType;
    } else {
      return scheme;
    }
  }

  public File toFile() {
    return new File(uri);
  }

  public String getUriString() {
    return uri.toString();
  }

  public boolean isOwner(String user) {
    return owner.equals(user);
  }

  public FsPath getParent() {
    String path = uri.getPath();
    int lastSlash = path.lastIndexOf('/');
    int start = startPositionWithoutWindowsDrive(path);
    if ((path.length() == start)
        || // empty path
        (lastSlash == start && path.length() == start + 1)) { // at root
      return null;
    }
    String parent;
    if (lastSlash == -1) {
      parent = CUR_DIR;
    } else {
      parent = path.substring(0, lastSlash == start ? start + 1 : lastSlash);
    }
    return new FsPath(uri.getScheme(), uri.getAuthority(), parent);
  }

  public long getModification_time() {
    return modification_time;
  }

  public void setModification_time(long modification_time) {
    this.modification_time = modification_time;
  }

  public long getAccess_time() {
    return access_time;
  }

  public void setAccess_time(long access_time) {
    this.access_time = access_time;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public boolean isdir() {
    return isdir;
  }

  public void setIsdir(boolean isdir) {
    this.isdir = isdir;
  }

  public String getPermissionString() {
    return permissionString;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setPermissions(Set<PosixFilePermission> permissions) throws IOException {
    //        this.permissions = FileSystem.permissionFormatted(permissions);
    this.permissions = permissions.toArray(new PosixFilePermission[] {});
    this.permissionString = PosixFilePermissions.toString(permissions);
  }

  public void setPermissionString(String permissionString) throws IOException {
    this.permissionString = permissionFormatted(permissionString);
    this.permissions =
        PosixFilePermissions.fromString(this.permissionString)
            .toArray(new PosixFilePermission[] {});
  }

  public String getPath() {
    if (WINDOWS && !"hdfs".equals(getFsType())) {
      return StringUtils.isNotBlank(uri.getAuthority())
          ? uri.getAuthority() + uri.getPath()
          : uri.getPath();
    }
    return uri.getPath();
  }

  public String getSchemaPath() {
    // local file system
    if (WINDOWS && !"hdfs".equals(getFsType())) {
      return getFsType() + "://" + uri.getAuthority() + uri.getPath();
    }

    if (Objects.isNull(uri.getAuthority())) {
      // eg: hdfs:///tmp/linkis use local hdfs
      return getFsType() + "://" + uri.getPath();
    } else {
      // eg: hdfs://nameNode:9000/tmp/linkis use specified hdfs
      // eg: oss://bucketName/tmp/linkis use OSS
      return getFsType() + "://" + uri.getAuthority() + uri.getPath();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append("{");
    sb.append("path=").append(uri.getPath());
    sb.append("; isDirectory=").append(isdir);
    if (!isdir) {
      sb.append("; length=").append(length);
    }
    sb.append("; modification_time=").append(modification_time);
    sb.append("; access_time=").append(access_time);
    sb.append("; owner=").append(owner);
    sb.append("; group=").append(group);
    sb.append("; permission=").append(permissions);
    sb.append("}");
    return sb.toString();
  }

  public static String permissionFormatted(String permission) throws IOException {
    if (!StringUtils.isNumeric(permission)) {
      return permission;
    }
    char[] ps = permission.toCharArray();
    return permissionFormatted(ps[0]) + permissionFormatted(ps[1]) + permissionFormatted(ps[2]);
  }

  public static String permissionFormatted(char i) throws IOException {
    int in = Integer.parseInt(String.valueOf(i));
    switch (in) {
      case 0:
        return "---";
      case 1:
        return "--x";
      case 2:
        return "-w-";
      case 3:
        return "-wx";
      case 4:
        return "r--";
      case 5:
        return "r-x";
      case 6:
        return "rw-";
      case 7:
        return "rwx";
      default:
        throw new IOException("Incorrent permission number " + in);
    }
  }

  public static FsPath getFsPath(String path, String... more) {
    String realPath = Paths.get(path, more).toFile().getPath();
    return new FsPath(realPath);
  }
}
