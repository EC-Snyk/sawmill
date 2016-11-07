package io.logz.sawmill;

import io.logz.sawmill.utilities.JsonUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public class Doc {

    private final Map<String, Object> source;
    private final Map<String, Object> metadata;

    public Doc(Map<String, Object> source, Map<String, Object> metadata) {
        checkState(MapUtils.isNotEmpty(source), "source cannot be empty");
        this.source = source;
        this.metadata = metadata;
    }

    public Doc(Map<String, Object> source) {
        this(source, new HashMap<>());
    }

    public Map<String, Object> getSource() { return source; }

    public Map<String, Object> getMetadata() { return metadata; }

    public boolean fieldExists(String path) {
        Optional<Object> field = JsonUtils.getByPath(source, path);
        return field.isPresent();
    }

    public <T> T getField(String path) {
        Optional<Object> field = JsonUtils.getByPath(source, path);
        checkState(field.isPresent(), String.format("Couldn't resolve field in path [%s]", path));
        return (T) field.get();
    }

    public void addField(String path, Object value) {
        Map<String, Object> context = source;
        String[] pathElements = path.split("\\.");

        String leafKey = pathElements[pathElements.length - 1];

        for (int i=0; i< pathElements.length - 1; i++) {
            String pathElement = pathElements[i];
            if (context.containsKey(pathElement)) {
                context = (Map) context.get(pathElement);
            } else {
                Map<String, Object> newMap = new HashMap<>();
                context.put(pathElement, newMap);
                context = newMap;
            }
        }

        context.put(leafKey, value);
    }

    public void removeField(String path) {
        Map<String, Object> context = source;
        String[] pathElements = path.split("\\.");

        String leafKey = pathElements[pathElements.length - 1];

        if (pathElements.length > 1) {
            String pathWithoutLeaf = path.substring(0, path.lastIndexOf("."));
            context = getField(pathWithoutLeaf);
        }

        checkState(context.containsKey(leafKey), String.format("Couldn't resolve field in path [%s]", path));
        context.remove(leafKey);
    }

    public void appendList(String path, Object value) {
        List<Object> list;
        if (!fieldExists(path)) {
            addField(path, new ArrayList<>());
        }

        list = getField(path);
        if (value instanceof List) {
            list.addAll((List)value);
        } else {
            list.add(value);
        }
    }

    public void removeFromList(String path, Object value) {
        List<Object> list = getField(path);
        if (value instanceof List) {
            list.removeAll((List)value);
        } else {
            list.remove(value);
        }
    }

    @Override
    public String toString() {
        return "Doc{" +
                "source=" + source +
                ", metadata=" + metadata +
                '}';
    }
}
