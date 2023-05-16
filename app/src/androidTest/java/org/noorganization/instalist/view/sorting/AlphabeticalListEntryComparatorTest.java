/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.view.sorting;

import android.test.AndroidTestCase;

import java.util.Locale;

public class AlphabeticalListEntryComparatorTest extends AndroidTestCase {

    private Locale defaultLocale;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
    }

    @Override
    public void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }

    public void testCompare() throws Exception {
      /*  ShoppingList list = new ShoppingList("_TEST_list");
        Product productWithA = new Product("Aubergine", null);
        Product productWithAUml = new Product("Äpfel", null);
        Product productWithB = new Product("Banane", null);
        Product productWithUUml = new Product("Überzug", null);
        ListEntry listEntryA = new ListEntry(list, productWithA, 1.0f);
        ListEntry listEntryAUml = new ListEntry(list, productWithAUml, 1.0f);
        ListEntry listEntryB = new ListEntry(list, productWithB, 1.0f);
        ListEntry listEntryUUml = new ListEntry(list, productWithUUml, 1.0f);

        ListEntryItemWrapper listEntryItemWrapperA = new ListEntryItemWrapper(listEntryA);
        ListEntryItemWrapper listEntryItemWrapperAUml = new ListEntryItemWrapper(listEntryAUml);
        ListEntryItemWrapper listEntryItemWrapperB = new ListEntryItemWrapper(listEntryB);
        ListEntryItemWrapper listEntryItemWrapperUUml = new ListEntryItemWrapper(listEntryUUml);

        Comparator<ListEntryItemWrapper> comp = new AlphabeticalListEntryComparator();
        assertEquals(0, comp.compare(listEntryItemWrapperA, listEntryItemWrapperA));
        assertTrue(comp.compare(listEntryItemWrapperAUml, listEntryItemWrapperA) < 0);
        assertTrue(comp.compare(listEntryItemWrapperB, listEntryItemWrapperA) > 0);
        assertTrue(comp.compare(listEntryItemWrapperB, listEntryItemWrapperAUml) > 0);
        assertTrue(comp.compare(listEntryItemWrapperUUml, listEntryItemWrapperAUml) > 0);
        assertTrue(comp.compare(listEntryItemWrapperUUml, listEntryItemWrapperB) > 0);
        */
    }
}