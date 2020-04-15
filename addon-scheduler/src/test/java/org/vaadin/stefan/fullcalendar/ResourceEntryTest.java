package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResourceEntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final LocalDateTime DEFAULT_END = DEFAULT_START.plusDays(1);
    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(FullCalendarSchedulerTest.COMPONENT_HTMLS);
    }

    @Test
    void testNoArgsConstructor() {
        Entry entry = new ResourceEntry();

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test if is editable
        Assertions.assertTrue(entry.isEditable());
    }

    @Test
    void testIdArgConstructor() {
        Entry entry = new ResourceEntry(null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        entry = new Entry("1");
        Assertions.assertEquals("1", entry.getId());
    }

    @Test
    void testFullArgsConstructor() {
        Entry entry;

        // test optional parameters
        entry = createResourceEntry(null, null, null, null, false, false, null, null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test field values after construction - all params
        entry = createResourceEntry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(DEFAULT_DESCRIPTION, entry.getDescription());

        // test null color when set empty
        Assertions.assertNull(createResourceEntry(null, null, null, null, false, false, "", null).getColor());
    }

    private static Entry createResourceEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        ResourceEntry entry = new ResourceEntry(id);

        entry.setTitle(title);
        entry.setStart(start != null ? start.toInstant(ZoneOffset.UTC) : null);
        entry.setEnd(end != null ? end.toInstant(ZoneOffset.UTC) : null);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setDescription(description);
        entry.setColor(color);

        return entry;
    }

    /**
     * Checks an original entry and the json based variant for equal fields, that can be changed by json.
     * @param expected expected entry
     * @param actual actual entry
     */
    static void assertFullEqualsByJsonAttributes(ResourceEntry expected, ResourceEntry actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getTitle(), actual.getTitle());
        Assertions.assertEquals(expected.getStart(), actual.getStart());
        Assertions.assertEquals(expected.getEnd(), actual.getEnd());
        Assertions.assertEquals(expected.isAllDay(), actual.isAllDay());
        Assertions.assertEquals(expected.isEditable(), actual.isEditable());
        Assertions.assertEquals(expected.getColor(), actual.getColor());
        Assertions.assertEquals(expected.getResources(), actual.getResources());
    }

    @Test
    void testToJson() {
        ResourceEntry entry = new ResourceEntry();
        Set<Resource> resources = new LinkedHashSet<>(Arrays.asList(new Resource(), new Resource(), new Resource()));
        entry.addResources(resources);

        JsonObject jsonObject = entry.toJson(); // rest of toJson is asserted in basis tests

        Assertions.assertTrue(jsonObject.get("resourceIds") instanceof JsonArray);

        JsonArray array = jsonObject.get("resourceIds");
        Set<String> jsonResourceIds = new LinkedHashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            jsonResourceIds.add(array.getString(i));
        }
        Assertions.assertEquals(resources.stream().map(Resource::getId).collect(Collectors.toSet()), jsonResourceIds);
    }

    @Test
    void testUpdateResourceEntryFromJson() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        Resource resource1 = new Resource("1", "1", null);
        Resource resource2 = new Resource("2", "2", null);
        Resource resource3 = new Resource("3", "3", null);
        calendar.addResources(resource1, resource2, resource3);

        ResourceEntry entry = new ResourceEntry();
        entry.setCalendar(calendar);

        Set<Resource> resourceList = new LinkedHashSet<>(Arrays.asList(resource1, resource2));
        entry.addResources(resourceList);

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", entry.getId());

        // test basic data
        jsonObject.put("title", DEFAULT_TITLE);
        jsonObject.put("start", DEFAULT_START.toString());
        jsonObject.put("end", DEFAULT_END.toString());
        jsonObject.put("allDay", false);
        jsonObject.put("editable", true);
        jsonObject.put("color", DEFAULT_COLOR);
        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object

        Assertions.assertNotNull(entry);
        Assertions.assertNotNull(jsonObject);
        entry.update(jsonObject);

        Assertions.assertEquals(jsonObject.getString("id"), entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertFalse(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(resourceList, entry.getResources()); // should not have changed yet

        // test resource changes
        // new resource
        jsonObject.put("newResource", "3");
        entry.update(jsonObject);
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(resource1, resource2, resource3)), entry.getResources());

        // remove resource
        jsonObject.remove("newResource");
        jsonObject.put("oldResource", "3");
        entry.update(jsonObject);
        Assertions.assertEquals(resourceList, entry.getResources());

        // change resource
        jsonObject.put("oldResource", "2");
        jsonObject.put("newResource", "3");
        entry.update(jsonObject);
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(resource1, resource3)), entry.getResources());

        Assertions.assertNull(entry.getDescription()); // should not be affected by json
    }

}
