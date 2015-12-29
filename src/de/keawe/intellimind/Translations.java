package de.keawe.intellimind;
import java.util.Locale;
import java.util.TreeMap;


public class Translations {
	
	private static TreeMap<String, String> trans=loadTranslations().getTranslations();
	
	public static String get(String key){		
		if (trans.containsKey(key)) return trans.get(key);
		return key;
	}

	public static String get(String key, Object insert) {
		String result=get(key);
		if (result==null) result=key;
		if (insert instanceof Object []){
			Object[] oarray = (Object[])insert;
			for (Object o:oarray){
				result=result.replaceFirst("#", string(o));
			}
			return result;
		}		
		return result.replace("#", string(insert));
	}
	
	private static Translation loadTranslations() {		
		String locale=Locale.getDefault().getLanguage().toUpperCase();
		System.out.print("Loading translation for "+locale+"...");
		Translation trans;
		try {
			trans = (Translation) Translation.class.getClassLoader().loadClass("de.keawe.intellimind.i3.Translation"+locale).newInstance();
			System.out.println("success.");
			return trans;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Failed. Using en.");
		return new Translation();
	}

	private static String string(Object insert) {
		if (insert==null) return "null";
	  return insert.toString();
  }


}
