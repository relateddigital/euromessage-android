package euromsg.com.euromobileandroid.callback;

import java.util.List;

import euromsg.com.euromobileandroid.model.Message;

public interface PushMessageInterface {
    void success(List<Message> pushMessages);

    void fail(String errorMessage);
}
