/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.test.content;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.extended.NamedCollectionConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.Mapper;

import org.catrobat.catroid.content.ListWithoutDuplicates;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.formulaeditor.UserList;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.io.BackwardCompatibleCatrobatLanguageXStream;
import org.catrobat.catroid.io.XStreamListWithoutDuplicatesConverter;
import org.catrobat.catroid.io.XstreamSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ListWithoutDuplicatesSerializationTest<T> {

	//@XStreamConverter(value=CollectionConverter.class, types={ArrayList.class}, useImplicitType = false)
	private List<UserVariable> userVariableListWithoutDuplicates;

	//@XStreamConverter(value=CollectionConverter.class, types={ArrayList.class}, useImplicitType = false)
	private List<UserVariable> emptyListWithoutDuplicates;

	private List<UserVariable> userVariableArrayList;

	private List<UserVariable> emptyArrayList;

	BackwardCompatibleCatrobatLanguageXStream xStream;

	private static class ClassA {
		public List<Integer> member = new ArrayList<>();

		public ClassA() {
			member.addAll(Arrays.asList(1, 2, 3));
		}
	}

	private static class ClassB {
		public List<Integer> member = new ListWithoutDuplicates<>();

		public ClassB() {
			member.addAll(Arrays.asList(1, 2, 3));
		}
	}

	@Before
	public void setUp() {
		List<UserVariable> userVariableCollection = Arrays.asList(new UserVariable("variable_x"),
				new UserVariable("variable_y"),
				new UserVariable("variable_z"));

		userVariableListWithoutDuplicates = new ListWithoutDuplicates<>();
		userVariableArrayList = new ArrayList<>();
		userVariableListWithoutDuplicates.addAll(userVariableCollection);
		userVariableArrayList.addAll(userVariableCollection);

		emptyListWithoutDuplicates = new ListWithoutDuplicates<>();
		emptyArrayList = new ArrayList<>();

		xStream = new BackwardCompatibleCatrobatLanguageXStream(new PureJavaReflectionProvider());
		xStream.registerConverter(new XStreamListWithoutDuplicatesConverter(xStream.getMapper()));
		//xStream.addDefaultImplementation(ArrayList.class, ListWithoutDuplicates.class);
	}

	@Test
	public void testSerializationWithEmptyLists() {
		String xmlOfListWithoutDuplicates = xStream.toXML(emptyListWithoutDuplicates);
		String xmlOfArrayList = xStream.toXML(emptyArrayList);


		assertEquals(xmlOfArrayList, xmlOfListWithoutDuplicates);
	}

	@Test
	public void testSerializationWithContentInLists() {
		String xmlOfEmptyListWithoutDuplicates = xStream.toXML(userVariableListWithoutDuplicates);
		String xmlOfEmptyArrayList = xStream.toXML(userVariableArrayList);

		assertEquals(xmlOfEmptyArrayList, xmlOfEmptyListWithoutDuplicates);
	}

	@Test
	public void testSerializationClass() {
		ClassA classA = new ClassA();
		ClassB classB = new ClassB();
		String arrayList = xStream.toXML(classA);
		String withoutDuplicates = xStream.toXML(classB);

		classA = (ClassA)xStream.fromXML(arrayList);
		classB = (ClassB)xStream.fromXML(withoutDuplicates);



		assertEquals(arrayList, withoutDuplicates);
	}


	/*@Test
	public void testSerializationOfListWithoutDuplicatesUserVariables() throws IOException, ClassNotFoundException {
		byte[] serializedUserVariablesWithoutDuplicates = serialize(userVariableListWithoutDuplicates);
		byte[] serializedUserVariablesArrayList = serialize(userVariableArrayList);

		Object deserializedUserVariablesWithoutDuplicates = deserialize(serializedUserVariablesWithoutDuplicates);
		Object deserializedUserVariablesArrayList = deserialize(serializedUserVariablesArrayList);

		assertTrue(deserializedUserVariablesWithoutDuplicates instanceof ListWithoutDuplicates);
		assertEquals(deserializedUserVariablesWithoutDuplicates, userVariableListWithoutDuplicates);
		assertEquals(deserializedUserVariablesWithoutDuplicates, deserializedUserVariablesArrayList);
	}

	@Test
	public void testSerializationOfListWithoutDuplicatesUserLists() throws IOException, ClassNotFoundException {
		byte[] serializedUserListsWithoutDuplicates = serialize(userListsWithoutDuplicates);
		byte[] serializedUserListsArrayList = serialize(userListsArrayList);

		Object deserializedUserListsWithoutDuplicates = deserialize(serializedUserListsWithoutDuplicates);
		Object deserializedUserListsArrayList = deserialize(serializedUserListsArrayList);

		assertTrue(deserializedUserListsWithoutDuplicates instanceof ListWithoutDuplicates);
		assertEquals(deserializedUserListsWithoutDuplicates, userListsWithoutDuplicates);
		assertEquals(deserializedUserListsWithoutDuplicates, deserializedUserListsArrayList);
	}


	private static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS);
		objectOS.writeObject(obj);
		return byteArrayOS.toByteArray();
	}

	private static Object deserialize(byte[] byteArray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(byteArray);
		ObjectInputStream objectIS = new ObjectInputStream(byteArrayIS);
		return objectIS.readObject();
	}*/

}
