package org.stevew;

/**
 * Created by estebanwasinger on 12/4/14.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stevew.exceptions.ChannelNotFoundException;
import org.stevew.exceptions.UserNotFoundException;
import org.stevew.model.User;
import org.stevew.model.channel.Channel;
import org.stevew.model.chat.Message;
import org.stevew.model.chat.MessageResponse;
import org.stevew.model.chat.attachment.ChatAttachment;
import org.stevew.model.file.FileUploadResponse;
import org.stevew.model.group.Group;
import org.stevew.model.im.DirectMessageChannel;
import org.stevew.model.im.DirectMessageChannelCreationResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SlackClient {

    private static final String NAME = "name";
    private static final String USER = "user" ;
    private static final String CHANNEL = "channel" ;
    private static final String CHANNELS = "channels";
    private static final String OK = "OK";
    private static final String TS = "TS" ;

    private String token;
    private Gson mapper;

    public SlackClient(String token) {
        this.token = token;
        mapper = new Gson();
    }

    //******************
    // Auth methods
    //******************

    public String testAuth() {
        return RestUtils.sendRequest(getURL(Operations.AUTH_TEST));
    }

    public Boolean isConnected() {
        String output = RestUtils.sendRequest(getURL(Operations.AUTH_TEST));
        JSONObject slackResponse = new JSONObject(output);
        return slackResponse.getBoolean(OK);
    }

    //******************
    // Channel methods
    //******************

    public List<Channel> getChannelList() {

        List<Channel> list = new ArrayList<Channel>();
        SlackRequest request = createAuthorizedRequest().setOperation(Operations.CHANNELS_LIST).enablePretty();
        String output = RestUtils.sendRequest(request);
        JSONArray channels = (JSONArray) new JSONObject(output).get(CHANNELS);

        for (int i = 0; i < channels.length(); i++) {
            JSONObject channel = (JSONObject) channels.get(i);
            Channel newChannel = mapper.fromJson(channel.toString(), Channel.class);
            list.add(newChannel);
        }
        return list;
    }

    public Boolean leaveChannel(String channelId) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_LEAVE);
        request.addArgument(CHANNEL, channelId);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Channel getChannelById(String id) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_INFO).addArgument(CHANNEL, id);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), Channel.class);
    }

    public List<Message> getChannelHistory(String channelId, String latest, String oldest, String count) {
        return getMessages(channelId, latest, oldest, count, Operations.CHANNELS_HISTORY);
    }

    public Channel createChannel(String channelName) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_CREATE);
        request.addArgument(NAME, channelName);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), Channel.class);
    }

    public Channel renameChannel(String channelId, String newName) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_RENAME);
        request.addArgument(CHANNEL, channelId);
        request.addArgument(NAME, newName);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), Channel.class);
    }

    public Channel joinChannel(String channelName) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_JOIN);
        request.addArgument(NAME, channelName);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), Channel.class);
    }

    public Channel getChannelByName(String name) {
        List<Channel> list = getChannelList();
        for (Channel channel : list) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        throw new ChannelNotFoundException("Channel: " + name + " does not exist.");
    }

    public Boolean setChannelPurpose(String channelID, String purpose) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_SETPURPOSE);
        request.addArgument(CHANNEL, channelID);
        request.addArgument("purpose", purpose);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean setChannelTopic(String channelID, String topic) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_SETTOPIC);
        request.addArgument(CHANNEL, channelID);
        request.addArgument("topic", topic);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean markViewChannel(String channelID, String timeStamp) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_MARK);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(TS, timeStamp);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean kickUserFromChannel(String channelID, String user){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_KICK);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(USER, user);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean inviteUserToChannel(String channelID, String user){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_INVITE);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(USER, user);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean unarchiveChannel(String channelID){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_UNARCHIVE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean archiveChannel(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHANNELS_ARCHIVE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    //******************
    // User methods
    //******************

    public User getUserInfo(String id) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.USER_INFO);
        request.addArgument(USER, id);

        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(USER);
        return mapper.fromJson(slackResponse.toString(), User.class);
    }

    public List<User> getUserList() {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.USER_LIST);

        String output = RestUtils.sendRequest(request);

        JSONArray slackResponse = (JSONArray) new JSONObject(output).get("members");
        Type listType = new TypeToken<ArrayList<User>>() {
        }.getType();
        return mapper.fromJson(slackResponse.toString(), listType);
    }

    public User getUserInfoByName(String username) throws UserNotFoundException {
        List<User> list = getUserList();
        for (User user : list) {
            if (user.getName().equals(username)) {
                return user;
            }
        }
        throw new UserNotFoundException("The user: " + username + " does not exist, please check the name!");
    }

    //******************
    // Chat methods
    //******************

    public MessageResponse sendMessage(String message, String channelId, String username, String iconUrl, Boolean asUser) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHAT_POSTMESSAGE);
        request.addArgument(CHANNEL, channelId);
        request.addArgument("text", message);
        request.addArgument("username", username);
        request.addArgument("icon_url", iconUrl);
        request.addArgument("as_user", String.valueOf(asUser));
        String output = RestUtils.sendRequest(request);
        return mapper.fromJson(output, MessageResponse.class);
    }

    public MessageResponse sendMessageWithAttachment(String message, String channelId, String username, String iconUrl,ChatAttachment chatAttachment, Boolean asUser) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHAT_POSTMESSAGE);
        request.addArgument(CHANNEL, channelId);
        request.addArgument("text", message);
        request.addArgument("username", username);
        request.addArgument("icon_url", iconUrl);
        request.addArgument("as_user", String.valueOf(asUser));
        ArrayList<ChatAttachment> chatAttachmentArrayList = new ArrayList<ChatAttachment>();
        chatAttachmentArrayList.add(chatAttachment);
        request.addArgument("attachments",mapper.toJson(chatAttachmentArrayList));
        System.out.println(mapper.toJson(chatAttachmentArrayList));
        String output = RestUtils.sendRequest(request);
        return mapper.fromJson(output, MessageResponse.class);
    }

    public Boolean deleteMessage(String timeStamp, String channelId) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHAT_DELETE);
        request.addArgument(CHANNEL, channelId);
        request.addArgument(TS, timeStamp);
        String output = RestUtils.sendRequest(request);
        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean updateMessage(String timeStamp, String channelId, String message) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.CHAT_UPDATE);
        request.addArgument(CHANNEL, channelId);
        request.addArgument("text", message);
        request.addArgument(TS, timeStamp);
        String output = RestUtils.sendRequest(request);
        return new JSONObject(output).getBoolean(OK);
    }

    //******************
    // IM methods
    //******************

    public DirectMessageChannelCreationResponse openDirectMessageChannel(String userId) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.IM_OPEN);
        request.addArgument(USER, userId);
        String output = RestUtils.sendRequest(request);
        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), DirectMessageChannelCreationResponse.class);
    }

    public List<DirectMessageChannel> getDirectMessageChannelsList() {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.IM_LIST);
        String output = RestUtils.sendRequest(request);
        JSONArray slackResponse = (JSONArray) new JSONObject(output).get("ims");
        Type listType = new TypeToken<ArrayList<DirectMessageChannel>>() {
        }.getType();
        return mapper.fromJson(slackResponse.toString(), listType);
    }

    public List<Message> getDirectChannelHistory(String channelId, String latest, String oldest, String count) {
        return getMessages(channelId, latest, oldest, count, Operations.IM_HISTORY);
    }

    public Boolean markViewDirectMessageChannel(String channelID, String timeStamp) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.IM_MARK);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(TS, timeStamp);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean closeDirectMessageChannel(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.IM_CLOSE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }


    //******************
    // Group methods
    //******************


    public List<Message> getGroupHistory(String channelId, String latest, String oldest, String count) {
        return getMessages(channelId, latest, oldest, count, Operations.GROUPS_HISTORY);
    }

    public List<Group> getGroupList() {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_LIST);
        String output = RestUtils.sendRequest(request);
        JSONArray slackResponse = (JSONArray) new JSONObject(output).get("groups");
        Type listType = new TypeToken<ArrayList<Group>>() {
        }.getType();
        return mapper.fromJson(slackResponse.toString(), listType);
    }

    public Group createGroup(String name) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_CREATE);
        request.addArgument(NAME, name);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get("group");
        return mapper.fromJson(slackResponse.toString(), Group.class);
    }

    public Boolean openGroup(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_OPEN);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean leaveGroup(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_LEAVE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean archiveGroup(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_ARCHIVE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean setGroupPurpose(String channelID, String purpose) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_SETPORPUSE);
        request.addArgument(CHANNEL, channelID);
        request.addArgument("purpose", purpose);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean setGroupTopic(String channelID, String topic) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_SETTOPIC);
        request.addArgument(CHANNEL, channelID);
        request.addArgument("topic", topic);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean closeGroup(String channelID) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_CLOSE);
        request.addArgument(CHANNEL, channelID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean markViewGroup(String channelID, String timeStamp) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_MARK);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(TS, timeStamp);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean kickUserFromGroup(String channelID, String user){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_KICK);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(USER, user);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean inviteUserToGroup(String channelID, String user){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_INVITE);
        request.addArgument(CHANNEL, channelID);
        request.addArgument(USER, user);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Boolean unarchiveGroup(String groupID){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_UNARCHIVE);
        request.addArgument(CHANNEL, groupID);
        String output = RestUtils.sendRequest(request);

        return new JSONObject(output).getBoolean(OK);
    }

    public Channel renameGroup(String channelId, String newName) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.GROUPS_RENAME);
        request.addArgument(CHANNEL, channelId);
        request.addArgument(NAME, newName);
        String output = RestUtils.sendRequest(request);

        JSONObject slackResponse = (JSONObject) new JSONObject(output).get(CHANNEL);
        return mapper.fromJson(slackResponse.toString(), Channel.class);
    }


    //******************
    // File methods
    //******************
    //TODO -- Delete duplicated code
    public FileUploadResponse sendFile(String channelId, String fileName, String fileType, String title, String initialComment, InputStream file){
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.FILES_UPLOAD);
        request.addArgument(CHANNELS, channelId);
        request.addArgument("filename", fileName);
        request.addArgument("filetype", fileType);
        request.addArgument("title", title);
        request.addArgument("initial_comment", initialComment);


        String stringResponse = RestUtils.sendAttachmentRequest(request, file);

        return mapper.fromJson(new JSONObject(stringResponse).getJSONObject("file").toString(),FileUploadResponse.class);
    }

    public FileUploadResponse sendFile(String channelId, String fileName, String fileType, String title, String initialComment, String filePath) throws IOException {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(Operations.FILES_UPLOAD);
        request.addArgument(CHANNELS, channelId);
        request.addArgument("filename", fileName);
        request.addArgument("filetype", fileType);
        request.addArgument("title", title);
        request.addArgument("initial_comment", initialComment);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File " + file.getAbsolutePath() + " does not exist!");
        }

        String stringResponse = RestUtils.sendAttachmentRequest(request, file);

        return mapper.fromJson(new JSONObject(stringResponse).getJSONObject("file").toString(),FileUploadResponse.class);
    }

    //******************
    // Util methods
    //******************

    public List<Message> getMessages(String channelId, String latest, String oldest, String count, String operation) {
        SlackRequest request = createAuthorizedRequest();
        request.setOperation(operation);
        request.addArgument(CHANNEL, channelId);
        request.addArgument("latest", latest);
        request.addArgument("oldest", oldest);
        request.addArgument("count", count);
        String output = RestUtils.sendRequest(request);
        JSONArray slackResponse = (JSONArray) new JSONObject(output).get("messages");
        Type listType = new TypeToken<ArrayList<Message>>() {
        }.getType();
        return mapper.fromJson(slackResponse.toString(), listType);
    }

    private String getURL(String operation) {
        return "https://slack.com/api/" + operation + "?token=" + token;
    }

    private SlackRequest createAuthorizedRequest() {
        return RestUtils.newRequest(token);
    }
}
