package centralworks.spawners.commons.database.specifications;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.Storable;
import centralworks.spawners.commons.di.InjectBuilder;
import centralworks.spawners.commons.di.services.ConnectionService;
import com.google.common.collect.Lists;
import lombok.Data;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Data
public class AbstractDAO<T extends Storable<T>> {

    private final Class<T> clazz;
    private final Properties properties;
    @Inject
    private Connection connection;

    public AbstractDAO(Class<T> clazz, Properties properties) {
        this.clazz = clazz;
        this.properties = properties;
        new InjectBuilder<>(this, ConnectionService.class);
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public String getKey() {
        return getProperty(PropertyType.KEY_NAME.getId());
    }

    public String getKeyDatatype() {
        return getProperty(PropertyType.KEY_DATATYPE.getId());
    }

    public Boolean isAutoIncrement() {
        return Boolean.valueOf(getProperty(PropertyType.KEY_AUTOINCREMENT.getId()));
    }

    public String getTable() {
        return getProperty(PropertyType.TABLE_NAME.getId());
    }

    public void createTable() {
        try {
            final String query = "CREATE TABLE IF NOT EXISTS " + getTable() +
                    " (" + getKey() + " " + getKeyDatatype() + " " + (isAutoIncrement() ? "AUTO_INCREMENT " : "") + ", value LONGTEXT, PRIMARY KEY(" + getKey() + "))";
            final PreparedStatement st = connection.prepareStatement(query);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(T object) {
        try {
            final String value = Main.getGson().toJson(object);
            final String query = "INSERT INTO " + getTable() + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + getKey() + " = ?, value = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setObject(1, object.getIdentifier());
            st.setObject(3, object.getIdentifier());
            st.setString(2, value);
            st.setString(4, value);
            st.executeUpdate();
            if (isAutoIncrement()) {
                final ResultSet rs = st.getGeneratedKeys();
                final int anInt = rs.getInt(1);
                object.setIdentifier(anInt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        new AbstractDTO<>(getClazz()).findAllFiles().forEach(this::save);
    }

    public T load(String id) {
        try {
            final String query = "SELECT * FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            final ResultSet rs = st.executeQuery();
            rs.next();
            return Main.getGson().fromJson(rs.getString("value"), getClazz());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T load(Integer id) {
        try {
            final String query = "SELECT * FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            final ResultSet rs = st.executeQuery();
            rs.next();
            return Main.getGson().fromJson(rs.getString("value"), getClazz());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> loadAll() {
        final List<T> list = Lists.newArrayList();
        try {
            final String query = "SELECT * FROM " + getTable();
            final PreparedStatement st = connection.prepareStatement(query);
            final ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(Main.getGson().fromJson(rs.getString("value"), getClazz()));
            }
            list.forEach(t -> t.query().commit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean exists(String id) {
        try {
            final String query = "SELECT * FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            final ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean exists(Integer id) {
        try {
            final String query = "SELECT * FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            final ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void delete(String id) {
        try {
            final String query = "DELETE FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Integer id) {
        try {
            final String query = "DELETE FROM " + getTable() + " WHERE " + getKey() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            final String query = "DELETE FROM " + getTable();
            final PreparedStatement st = connection.prepareStatement(query);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<T> findAll() {
        final List<T> list = Lists.newArrayList();
        try {
            final String query = "SELECT * FROM " + getTable();
            final PreparedStatement st = connection.prepareStatement(query);
            final ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(Main.getGson().fromJson(rs.getString("value"), getClazz()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
