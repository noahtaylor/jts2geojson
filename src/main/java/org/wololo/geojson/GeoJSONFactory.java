package org.wololo.geojson;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

public class GeoJSONFactory {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static GeoJSON create(String json) {
		try {
			JsonNode node = mapper.readTree(json);
			String type = node.get("type").asText();
			if (type.equals("FeatureCollection")) {
				return readFeatureCollection(node);
			} else if (type.equals("Feature")) {
				return readFeature(node);
			} else {
				return readGeometry(node, type);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * read a feature colleciton from a JsonNode
	 *
	 * @param node
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static FeatureCollection readFeatureCollection(JsonNode node) throws JsonParseException, JsonMappingException, IOException,
    ClassNotFoundException {
		Iterator<JsonNode> it = node.get("features").iterator();
		// iterate through all the features and read them one at a time
		List<Feature> features = new ArrayList<Feature>();
		while (it.hasNext()) {
			JsonNode jFeature = it.next();
			features.add(readFeature(jFeature));
		}
        
		return new FeatureCollection(features.toArray(new Feature[features.size()]));
	}
	
	private static Feature readFeature(JsonNode node)
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException {
		JsonNode geometryNode = node.get("geometry");
		JavaType javaType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> properties = mapper.readValue(node.get("properties").traverse(), javaType);
		String type = geometryNode.get("type").asText();
		Geometry geometry = readGeometry(geometryNode, type);
		return new Feature(geometry, properties);
	}
	
	private static Geometry readGeometry(JsonNode node, String type)
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException {
		Geometry geometry = (Geometry) mapper.readValue(node.traverse(), Class.forName("org.wololo.geojson." + type));
		return geometry;
	}
}
