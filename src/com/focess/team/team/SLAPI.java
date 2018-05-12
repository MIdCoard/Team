/** SLAPI = Saving/Loading API
 
 * API for Saving and Loading Objects.
 
 * Everyone has permission to include this code in their plugins as they wish :)
 
 * @author Tomsik68<tomsik68@gmail.com>
 
 */
package com.focess.team.team;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
	@SuppressWarnings("unchecked")
	public static <T extends Object> T load(final String path) throws Exception {
		final ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path));
		final T result = (T) ois.readObject();
		ois.close();
		return result;
	}

	public static <T extends Object> void save(final T obj, final String path)
			throws Exception {
		final ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}
}
