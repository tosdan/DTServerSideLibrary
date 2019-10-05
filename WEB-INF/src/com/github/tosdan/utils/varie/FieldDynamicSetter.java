package com.github.tosdan.utils.varie;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * 
 * @author Daniele
 * @version 0.0.2-b2013-09-01
 */
public class FieldDynamicSetter {
	/**
	 * Esegue il set dei campi dell'oggetto con la reflection
	 * @param objInstance Oggetto per quale si vuole effettuare il set dinamico dei parametri
	 * @param paramsMap Mappa parametri: le chiavi devono essere i nomi dei campi dell'oggetto per valore il dato che si vuole impostare per ogni campo
	 */
	public static <T> void dynamicSetFileds( T objInstance, Map<String, Object> paramsMap) {
		if ( paramsMap != null && !paramsMap.isEmpty() ) {
			Field[] fields = objInstance.getClass().getDeclaredFields();
			for( int i = 0 ; i < fields.length ; i++ ) {

				Field currField = fields[i];
				currField.setAccessible(true);
				Object curFieldValueToSet = paramsMap.get( currField.getName() );
				
				if ( curFieldValueToSet != null ) {
					try {
						String currFieldType = currField.getType().getName();
						
						currField.set(objInstance, castToFieldType(curFieldValueToSet, currFieldType));
	
					} catch ( IllegalAccessException e ) {
						throw new IllegalStateException(e.getMessage(), e);
	
					}
				}
			}
		}
	}
	
	/**
	 * Per assicurarsi coerenza dei tipi viene fatto un controllo sulla possibilita' di fare
	 * un cast al tipo che l'oggetto si aspetterebbe per il proprio campo.
	 * @param valueToSet Valore cui il field deve essere impostato 
	 * @param currFieldType Tipo del field da impostare
	 * @return
	 */
	public static Object castToFieldType(Object valueToSet, String currFieldType) {
		currFieldType = currFieldType.toLowerCase();
		try {
			if ( valueToSet instanceof String ) {
	
				if ( currFieldType.equals("boolean") || currFieldType.equals("java.lang.boolean") ) {
					String valore = valueToSet.toString().toLowerCase();
					if (valore.equals("true") || valore.equals("false"))
						valueToSet = Boolean.valueOf(valueToSet.toString());
					
				} else if ( currFieldType.equals("int") || currFieldType.equals("java.lang.integer") ) {
					valueToSet = Integer.valueOf(valueToSet.toString());
	
				} else if ( currFieldType.equals("float") || currFieldType.equals("java.lang.float") ) {
					valueToSet = Float.valueOf(valueToSet.toString());
	
				} else if ( currFieldType.equals("double") || currFieldType.equals("java.lang.double") ) {
					valueToSet = Double.valueOf(valueToSet.toString());
					
				} else if ( currFieldType.equals("long") || currFieldType.equals("java.lang.long") ) {
					valueToSet = Long.valueOf(valueToSet.toString());
					
				} else if ( currFieldType.equals("short") || currFieldType.equals("java.lang.short") ) {
					valueToSet = Short.valueOf(valueToSet.toString());
					
				} else if ( currFieldType.equals("char") || currFieldType.equals("java.lang.character") ) {
					valueToSet = valueToSet.toString().toCharArray()[0];
					
				} else if ( currFieldType.equals("java.math.bigdecimal") ) {
					valueToSet = BigDecimal.valueOf(Long.valueOf(valueToSet.toString()));
					
				} else if ( currFieldType.equals("java.math.biginteger") ) {
					valueToSet = BigInteger.valueOf(Long.valueOf(valueToSet.toString()));
					
				}
			}
		} catch (Exception e) {
			// Il metodo java.lang.reflect.Field.set gestisce gia' molto bene e con messaggi  
			// di errore chiari il caso in cui il valore non corrisponda al tipo del campo.
		}
		return valueToSet;				
	}
}
