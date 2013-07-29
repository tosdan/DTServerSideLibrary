package com.github.tosdan.utils.varie;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class YamlUtils
{
	// Funzione di utilita' per convertire un oggetto Collection in un formato (YAML) per una stampa piu' leggibile
	public static <T extends Object> String toYamlBlock(T obj) {
		return toYaml( obj, DumperOptions.FlowStyle.BLOCK );
	}

	// Funzione di utilita' per convertire un oggetto Collection in un formato (YAML) per una stampa piu' leggibile
	public static <T extends Object> String toYamlFlow(T obj) {
		return toYaml( obj, DumperOptions.FlowStyle.FLOW );
	}
	
	// Funzione di utilita' per convertire un oggetto Collection in un formato (YAML) per una stampa piu' leggibile
	private static <T extends Object> String toYaml(T obj, FlowStyle f ) {	
		DumperOptions options = new DumperOptions();
//		options.setDefaultScalarStyle( ScalarStyle.LITERAL );
		options.setDefaultFlowStyle(f);
		Yaml y = new Yaml(options);
		
		return y.dump( obj );
	}
}
