package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

import java.util.UUID;

/**
 * Represents a Device. Used in the Onyx subsystem by the
 * {@link com.onyx.quadcopter.main.Controller}.
 * Each devices holds state about it's name, id, last recieved message
 * and if it has been initialized or not.
 * 
 * @author fred
 *
 */
public interface IDevice {
  
  /**
   * True if this device is initialized.
   * 
   * @return
   *    true if this device is initialized.
   */
  boolean isInitialized();
  
  /**
   * Get last Acl Message.
   * @return
   *    the last read ACL Message 
   *      processed by {@link #update(AclMessage) update}.
   */
  AclMessage getLastAclMessage();
  

  /**
   * Updated device is guarenteed to have at least one ACLMessage recieved.
   * Execute an update given the recieved ACLMessage msg.
   * 
   * @param msg the message to be updated with.
   */
  void update(final AclMessage msg);
  
  /**
   * Execute this device.
   */
  void execute();
  
  /**
   * Set the OLED Display text.
   * @param text
   *    the text to be displayed.
   */
  void setDisplay(final String text);
  
  /**
   * Get the ID of this IDevice.
   * @return
   *    the ID of this device.
   */
  DeviceId getId();
  
  /**
   * Send an ACL Message.
   * 
   * @param type
   *    the {@link MessageType} of this ACL Message
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   * @param uuid
   *    the {@link UUID} for this message
   */
  void sendMessage(final MessageType type, final DeviceId receiver, final String content,
      final double value, final ActionId action, final AclPriority priority, final UUID uuid);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param type
   *    the {@link MessageType} of this ACL Message
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   */
  void sendMessage(final MessageType type, final DeviceId receiver, final String content,
      final double value, final ActionId action, final AclPriority priority);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   */
  void sendMessage(final DeviceId receiver, final String content, final double value,
      final ActionId action, final AclPriority priority);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   */
  void sendMessage(final DeviceId receiver, final String content, final ActionId action,
      final AclPriority priority);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   */
  void sendMessage(
      final DeviceId receiver, final String content, final ActionId action);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param aclm
   *    the {@link AclMessage} to send.
   */
  void sendMessage(AclMessage aclm);
  
  
  /**
   * Send an ACL Message.
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   */
  void sendMessageHigh(final DeviceId receiver, final String content,
      final ActionId action);
  
  
  /**
   * Send an {@link AclMessage} with HIGH {@link AclPriority}.
   * 
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   */
  void sendMessageHigh(final DeviceId receiver, final String content, final double value,
      final ActionId action);
  
  
  /**
   * Send an {@link AclMessage} with {@link AclPriority.LOW}.
   * 
   * @param receiver
   *    the {@link DeviceId} of the desired reciever of this {@link AclMessage}
   * @param content
   *    the content of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   */
  void sendMessageLow(final DeviceId receiver,
      final String content, final ActionId action);
  
  
  /**
   * Send an ACL Message reply.
   * 
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   */
  void sendReply(final String content, final double value, final ActionId action,
      final AclPriority priority);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param content
   *    the content of this {@link AclMessage}
   * @param action
   *    the {@link ActionId} of this {@link AclMessage}
   * @param priority
   *    the {@link AclPriority} of this {@link AclMessage}
   */
  void sendReply(final String content,
      final ActionId action, final AclPriority priority);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param content
   *    the content of this {@link AclMessage}
   */
  void sendReply(final String content);
  
  
  /**
   * Send an ACL Message.
   * 
   * @param content
   *    the content of this {@link AclMessage}
   * @param value
   *    the double value of this {@link AclMessage}
   */
  void sendReply(final String content, final double value);
  
  /**
   * Initialize this {@link IDevice}.
   */
  void initialize();
  
  /**
   * Self Test, return true if this test was successful.
   * 
   * @return
   *    true if this test was successful.
   */
  boolean selfTest();
  
  /**
   * Shutdown this {@link IDevice}.
   */
  void shutdown();
}
