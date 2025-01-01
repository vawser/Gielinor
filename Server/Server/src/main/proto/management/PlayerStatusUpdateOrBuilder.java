// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Management.proto

package proto.management;

public interface PlayerStatusUpdateOrBuilder extends
    // @@protoc_insertion_point(interface_extends:management.PlayerStatusUpdate)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>required string username = 1;</code>
   * @return Whether the username field is set.
   */
  boolean hasUsername();
  /**
   * <code>required string username = 1;</code>
   * @return The username.
   */
  java.lang.String getUsername();
  /**
   * <code>required string username = 1;</code>
   * @return The bytes for username.
   */
  com.google.protobuf.ByteString
      getUsernameBytes();

  /**
   * <code>required int32 world = 2;</code>
   * @return Whether the world field is set.
   */
  boolean hasWorld();
  /**
   * <code>required int32 world = 2;</code>
   * @return The world.
   */
  int getWorld();

  /**
   * <code>required bool notifyFriendsOnly = 3;</code>
   * @return Whether the notifyFriendsOnly field is set.
   */
  boolean hasNotifyFriendsOnly();
  /**
   * <code>required bool notifyFriendsOnly = 3;</code>
   * @return The notifyFriendsOnly.
   */
  boolean getNotifyFriendsOnly();
}