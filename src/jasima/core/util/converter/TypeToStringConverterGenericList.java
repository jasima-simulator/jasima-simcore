package jasima.core.util.converter;

import java.awt.List;
import java.lang.reflect.Array;
import java.util.ArrayList;

import jasima.core.util.converter.ArgListTokenizer.TokenType;

public class TypeToStringConverterGenericList extends TypeToStringConverter {

	@Override
	public Class<?>[] handledTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> requiredType, String context, ClassLoader loader,
			String[] packageSearchPath) {
		boolean isList = List.class.isAssignableFrom(requiredType);
		assert requiredType.isArray() || isList;
		
		Class<?> componentType = List.class.isAssignableFrom(requiredType) ? Object.class : requiredType.getComponentType();
		
		TypeToStringConverter elementConverter = lookupConverter(componentType);

		ArrayList<Object> res = new ArrayList<>();
		
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.BRACKETS_OPEN);
		
		while (true) {
			TokenType t = tk.nextTokenNoWhitespace();
			if (t==TokenType.BRACKETS_CLOSE)
				break; // while
			else 
				tk.pushBackToken(); // let "elementConverter" handle it
			
			Object element = elementConverter.fromString(tk, componentType, context, loader, packageSearchPath);
			res.add(element);
			
			TokenType t2 = tk.nextTokenNoWhitespace();
			tk.assureTokenTypes(t2, TokenType.BRACKETS_CLOSE, TokenType.SEMICOLON);
			if (t2==TokenType.BRACKETS_CLOSE)
				tk.pushBackToken(); // will terminate while-loop
			else {
				// read next element
			}
		}
		
		T resObj;
		if (isList) {
			resObj = (T) List.class.cast(res);
		} else {
			Object[] listAsArray = res.toArray((Object[]) Array.newInstance(componentType, res.size()));
			resObj = (T) listAsArray;
		}
		
		return resObj;
	}

}
