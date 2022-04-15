package Server.Servlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {
    ConnectionFactory connectionFactory;
    Connection connection;
    String HOST = "172.31.1.65";
    int PORT = 5672;
    String USERNAME = "user";
    String PASSWORD = "password";

    public ChannelFactory() {
      this.connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(HOST);
      connectionFactory.setPort(PORT);
      connectionFactory.setVirtualHost("/");
      connectionFactory.setUsername(USERNAME);
      connectionFactory.setPassword(PASSWORD);
      try {
        this.connection = connectionFactory.newConnection();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }
    }

    @Override
    public Channel create() throws Exception {
      return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
      return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p, DestroyMode destroyMode) throws Exception {
      p.getObject().close();
    }
}
