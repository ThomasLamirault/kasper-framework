// ----------------------------------------------------------------------------
//  This file is part of the Kasper framework.
//
//  The Kasper framework is free software: you can redistribute it and/or 
//  modify it under the terms of the GNU Lesser General Public License as 
//  published by the Free Software Foundation, either version 3 of the 
//  License, or (at your option) any later version.
//
//  Kasper framework is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with the framework Kasper.  
//  If not, see <http://www.gnu.org/licenses/>.
// --
//  Ce fichier fait partie du framework logiciel Kasper
//
//  Ce programme est un logiciel libre ; vous pouvez le redistribuer ou le 
//  modifier suivant les termes de la GNU Lesser General Public License telle 
//  que publiée par la Free Software Foundation ; soit la version 3 de la 
//  licence, soit (à votre gré) toute version ultérieure.
//
//  Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS 
//  AUCUNE GARANTIE ; sans même la garantie tacite de QUALITÉ MARCHANDE ou 
//  d'ADÉQUATION à UN BUT PARTICULIER. Consultez la GNU Lesser General Public 
//  License pour plus de détails.
//
//  Vous devez avoir reçu une copie de la GNU Lesser General Public License en 
//  même temps que ce programme ; si ce n'est pas le cas, consultez 
//  <http://www.gnu.org/licenses>
// ----------------------------------------------------------------------------
// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.common.serde;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viadeo.kasper.test.platform.KasperMatcher;
import org.junit.Before;
import org.junit.Test;

import static com.viadeo.kasper.test.platform.KasperMatcher.equalTo;
import static org.junit.Assert.assertTrue;

public class KasperImmutabilityParanamerModuleITest {

    private ObjectMapper mapper;

    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new ImmutabilityModule());
    }

    @Test
    public void canSerDeser_withMutableObject_isOk() throws Exception {
        // Given
        final MutableObject mutableObject = new MutableObject("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(mutableObject);
        final MutableObject actualMutableObject = mapper.readValue(json, MutableObject.class);

        // Then
        assertTrue(equalTo(mutableObject).matches(actualMutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObject_isOk() throws Exception {
        // Given
        final ImmutableObject immutableObject = new ImmutableObject("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObject actualImmutableObject = mapper.readValue(json, ImmutableObject.class);

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObject_containingAnotherImmutableObject_isOk() throws Exception {
        // Given
        final ImmutableObjectContainingAnother immutableObject = new ImmutableObjectContainingAnother("foobar", 42, new ImmutableObject2("foobar", 42));

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObjectContainingAnother actualImmutableObject =
                mapper.readValue(json, ImmutableObjectContainingAnother.class);

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObjectUsingJacksonAnnotation_isOk() throws Exception {
        // Given
        final ImmutableObjectUsingJacksonAnnotation immutableObject =
                new ImmutableObjectUsingJacksonAnnotation("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObjectUsingJacksonAnnotation actualImmutableObject = mapper.readValue(
                json,
                ImmutableObjectUsingJacksonAnnotation.class
        );

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObjectWithSeveralConstructors_isOk() throws Exception {
        // Given
        final ImmutableObjectWithSeveralConstructors immutableObject =
                new ImmutableObjectWithSeveralConstructors("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObjectWithSeveralConstructors actualImmutableObject =
                mapper.readValue(json, ImmutableObjectWithSeveralConstructors.class);

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObjectUsingJacksonAnnotationWithSeveralConstructors_isOk() throws Exception {
        // Given
        final ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors immutableObject =
                new ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors actualImmutableObject =
                mapper.readValue(json, ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors.class);

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }

    @Test
    public void canSerDeser_withImmutableObjectWithSeveralConstructors2_isOk() throws Exception {
        // Given
        final ImmutableObjectWithSeveralConstructors2 immutableObject = new ImmutableObjectWithSeveralConstructors2("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        final ImmutableObjectWithSeveralConstructors2 actualImmutableObject = mapper.readValue(
                json,
                ImmutableObjectWithSeveralConstructors2.class
        );

        // Then
        assertTrue(equalTo(immutableObject).matches(actualImmutableObject));
    }


    @Test(expected = RuntimeException.class)
    public void canSerDeser_fromImmutableObject_usingTwoConstructorsAnnotatedWithJsonCreator_throwsException() throws Exception {
        // Given
        final ImmutableObjectUsingTwoJsonCreatorAnnotations immutableObject =
                new ImmutableObjectUsingTwoJsonCreatorAnnotations("foobar", 42);

        // When
        final String json = mapper.writeValueAsString(immutableObject);
        mapper.readValue(json, ImmutableObjectWithSeveralConstructors2.class);

        // Then throws exception
    }

    //--------------------------------------------------------------------------------

    public static class ImmutableObject {

        private final String label;
        private final Integer value;

        public ImmutableObject(final String label, final Integer value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }

    }

    public static class ImmutableObject2 {

        private final String label;
        private final Integer value;

        public ImmutableObject2(final String label, final Integer value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectContainingAnother {

        private final String label;
        private final Integer value;
        private final ImmutableObject2 object;

        public ImmutableObjectContainingAnother(
                final String label,
                final Integer value,
                final ImmutableObject2 object) {
            this.label = label;
            this.value = value;
            this.object = object;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }

        public ImmutableObject2 getObject() {
            return object;
        }
    }

    public static class ImmutableObjectUsingJacksonAnnotation {

        private final String label;
        private final Integer value;

        @JsonCreator
        public ImmutableObjectUsingJacksonAnnotation(@JsonProperty("label") final String label2,
                                                     @JsonProperty("value") final Integer value2) {
            this.label = label2;
            this.value = value2;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectUsingBadlyJacksonAnnotation {

        private final String label;
        private final Integer value;

        @JsonCreator
        public ImmutableObjectUsingBadlyJacksonAnnotation(@JsonProperty("label") final String label2,
                                                          final Integer value2) {
            this.label = label2;
            this.value = value2;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }

    }

    public static class ImmutableObjectWithSeveralConstructors {

        private final String label;
        private final Integer value;

        @JsonCreator
        public ImmutableObjectWithSeveralConstructors(@JsonProperty("label") final String label2,
                                                      @JsonProperty("value") final Integer value) {
            this.label = label2;
            this.value = value;
        }

        public ImmutableObjectWithSeveralConstructors(final String label) {
            this(label, null);
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors {

        private final String label;
        private final Integer value;

        @JsonCreator
        public ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors(@JsonProperty("label") final String label2,
                                                                            @JsonProperty("value") final Integer value) {
            this.label = label2;
            this.value = value;
        }

        @JsonIgnore
        public ImmutableObjectUsingJacksonAnnotationWithSeveralConstructors(final String label1) {
            this(label1, null);
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectWithSeveralConstructors2 {

        private final String label;
        private final Integer value;

        public ImmutableObjectWithSeveralConstructors2() {
            this("miam", null);
        }

        @JsonCreator
        public ImmutableObjectWithSeveralConstructors2(@JsonProperty("label") final String label2,
                                                       @JsonProperty("value") final Integer value) {
            this.label = label2;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectUsingTwoJsonCreatorAnnotations {

        private final String label;
        private final Integer value;

        @JsonCreator
        public ImmutableObjectUsingTwoJsonCreatorAnnotations(final String label2) {
            this(label2, null);
        }

        @JsonCreator
        public ImmutableObjectUsingTwoJsonCreatorAnnotations(@JsonProperty("label") final String label2,
                                                             @JsonProperty("value") final Integer value) {
            this.label = label2;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class ImmutableObjectUsingTwoConstructorsWithoutAnnotation {

        private final String label;
        private final Integer value;

        public ImmutableObjectUsingTwoConstructorsWithoutAnnotation(final Integer value, final String label2) {
            this(label2, value);
        }

        public ImmutableObjectUsingTwoConstructorsWithoutAnnotation(final String label2, final Integer value) {
            this.label = label2;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static class MutableObject {

        private String label;
        private Integer value;

        public MutableObject() {
        }

        public MutableObject(final String label, final Integer value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

}
