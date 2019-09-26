# FullCalendar integration
This addon is an integration of the FullCalendar (v4.3.1) as Flow component for Vaadin 14+. For a Vaadin 10-13 version 
 (built on FC 3.10.x) https://vaadin.com/directory/component/full-calendar-web-component.

Please also have a look at the demo for a live example and source code of how to integrate the FC. Not all
described events are handled visually currently.

For information about the FullCalendar (functionality, features, license information, etc.) visit https://fullcalendar.io/

If you want to use the FC Scheduler, please have a look at this addon: https://vaadin.com/directory/component/full-calendar-4-scheduler-web-component

## Addon Functionality
The following functions are currently implemented and available to use from server side:
- adding / updating / removing calendar items,
- switching between shown intervals (next month, previous month, etc.),
- goto a specific date or today,
- switch the calendar view (month, basic views for days and weeks, agenda views for days and weeks, list views for day to year),
- setting a locale to be used for displaying week days, formatting values, calculating the first day of the week, etc. (supported locales are provided as constant list)
- setting the first day of week to be shown (overrides the locale setting),
- show of week numbers
- limit max shown entries per day (except basic views)
- showing now indicator
- activating day / week numbers / names to be links
- setting a eventRender JS function from server side
- setting business hours information (multiple entries possible)
- creating recurring events
- setting / handling timezones and their offsets (by default the FC uses UTC times and dates)

- Event handling for
    - clicking an empty time spot in the calendar,
    - selecting a block of empty time spots in the calendar, 
    - clicking an entry,
    - moving an entry via drag and drop (event is fired on drop + changed time),
    - resizing an entry (event is fired after resize + changed time),
    - view rendered (i. e. to update a label of the shown interval)
    - clicking on limited entries link "+ X more"
    - clicking on a day's or week's number link (when activated)
    
- Model supports setting 
    - title, 
    - start / end / all day flag, 
    - color (html colors, like "#f00" or "red"), 
    - description (not shown via FC), 
    - editable / read only
    - rendering mode (normal, background, inversed background)
    - recurring data (day of week, start / end date and time)

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

## Examples
### Creating a basic instance and add an entry
```
// Create a new calendar instance and attach it to our layout
FullCalendar calendar = FullCalendarBuilder.create().build();
container.add(calendar);
container.setFlexGrow(1, calendar);

// Create a initial sample entry
Entry entry = new Entry();
entry.setTitle("Some event");
entry.setStart(LocalDate.now().withDayOfMonth(3).atTime(10, 0), calendar.getTimezone());
entry.setEnd(entry.getStart().plusHours(2), calendar.getTimezone());
entry.setColor("#ff3333");

calendar.add(entry);
```

### Show a dialog to create new entries or modify existing ones
```
/*
 * The day click event listener is called when a user clicks in an empty space inside of the 
 * calendar. Depending of if the clicked point was a day or time slot the event will provide the 
 * time details of the clicked point. With this info you can show a dialog to create a new entry.
 */
calendar.addTimeslotsSelectedListener((event) -> {
    // react on the selected timeslot, for instance create a new instance and let the user edit it 
    Entry entry = new Entry();

    entry.setStart(calendar.getTimezone().convertToUTC(event.getStartDateTime()));
    entry.setEnd(calendar.getTimezone().convertToUTC(event.getEndDateTime()));
    entry.setAllDay(event.isAllDay());

    entry.setColor("dodgerblue");

    // ... show and editor
});

/*
 * The entry click event listener is called when the user clicks on an existing entry. 
 * The event provides the clicked event which might be then opened in a dialog.
 */
calendar.addEntryClickListener(event -> /* ... */);
```

### Add, update or remove an entry
```
// ... create a form and binder to provide editable components to the user

HorizontalLayout buttons = new HorizontalLayout();
Button buttonSave;
if (newInstance) {
    buttonSave = new Button("Create", e -> {
        if (binder.validate().isOk()) {
            // add the entry to the calendar instance
            calendar.addEntry(entry);
        }
    });
} else {
    buttonSave = new Button("Save", e -> {
        if (binder.validate().isOk()) {
             // update an existing entry in the client side
             calendar.updateEntry(entry);
        }
    });
}       
buttons.add(buttonSave);

if (!newInstance) {
    Button buttonRemove = new Button("Remove", e -> calendar.removeEntry(entry));
    buttons.add(buttonRemove);
}
```

### Show the current shown time interval (e. g. month) with Vaadin components 
```
private void init() {
    // The element that should show the current interval. 
    HasText intervalLabel = new Span();

    // combo box to select a view for the calendar, like "monthly", "weekly", ...
    ComboBox<CalendarView> viewBox = new ComboBox<>("", CalendarViewImpl.values());
    viewBox.addValueChangeListener(e -> {
        CalendarView value = e.getValue();
        calendar.changeView(value == null ? CalendarViewImpl.MONTH : value);
    });
    viewBox.setValue(CalendarViewImpl.MONTH);

    /*
      * The view rendered listener is called when the view has been rendererd on client side 
      * and FC is aware of the current shown interval. Might be accessible more directly in 
      * future.
      */ 
    calendar.addDatesRenderedListener(event -> 
        LocalDate intervalStart = event.getIntervalStart();
        CalendarView cView = viewBox.getValue();

        String formattedInterval = ... // format the intervalStart based on cView 

        intervalLabel.setText(formattedInterval);
    });
}
```

### Example combinations of parent and calendar height settings.
```
// #1 setting a fixed height
calendar.setHeight(500);

// #2 setting a auto height - this is calculated by the w-h-ratio of the calendar
calendar.setHeightAuto();

// #3 calculate height by parent. parent is a block container.
calendar.setHeightByParent();
calendar.setSizeFull();

// #4 calculate height by parent + usage of css calc(). parent is a block container.
calendar.setHeightByParent();
calendar.getElement().getStyle().set("height", "calc(100vh - 450px)");

// #5 calculate height by parent. parent is a flex container.
calendar.setHeightByParent();
calendar.getElement().getStyle().set("flex-grow", "1");
```

### Modify FCs appearance by using css variables
1. Copy the styles.js from the github demo or create your own custom style file and place it in your
 applications frontend folder (e. g. frontend/styles/my-custom-full-calendar-styles.js)

An example file can be found from here:
https://github.com/stefanuebe/vaadin_fullcalendar/blob/master/demo/frontend/styles.js


2. Modify the styles as needed.
```
const $_documentContainer = document.createElement('template');
$_documentContainer.innerHTML = `
<custom-style>
    <style>
        html{
            /* light blue to be used instead of default light yellow*/
            --fc-unthemed_tdfc-today-background: #81DAF5 !important;
            
            /* and some fancy border */
            --fc_td-border-style: dotted !important;
            --fc_td-border-width: 2px !important;
        }
    </style>
</custom-style>
`;
document.head.appendChild($_documentContainer.content);
```

3. Use the styles file in your application.
```
@JsModule("./styles/my-custom-full-calendar-styles.js")
public class FullCalendarApplication extends ... {
    // ...
}
```

### Modifiy FC's appearance by using a custom class.
Create a custom component, that extends FullCalendar or FullCalendarScheduler. 
Override the static template method and reuse the parent's methods to create the basic styles.

The following example shows how a custom class extends the basic fc class and adds it's own styles. It will 
set the background of the "today" cell to red. 

Please note, that you also need a Java class using this Polymer class on the server side. The Scheduler is
working the same way, please have a look at its implementation for further details.

```
export class MyFullCalendar extends FullCalendar {
    static get template() {
        return html`
            ${this.templateCalendarCss}
            
            ${this.templateCustomCalendarCss}
        
            ${this.templateElementCss}
            ${this.templateContainer}
        `;
    }

    static get templateCustomCalendarCss() {
        return html`
        <style>
             .fc-unthemed td.fc-today {
               background: red;
             }
        </style>
        `;
    }
}

customElements.define('my-full-calendar', MyFullCalendar);
```

### Modifying eventRender from server side
// The given string will be interpreted as js function on client side
// and attached as eventRender callback. 
// Make sure, that it does not contain any harmful code.

calendar.setEntryRenderCallback("" +
        "function(event, element) {" +
        "   console.log(event.title + 'X');" +
        "   element.css('color', 'red');" +
        "   return element; " +
        "}");
        
### Creating a subclass of FullCalendar for custom mods
1. Create a new polymer template (e.g. webapp/frontend/my-full-calendar.html):

<link rel="import" href="bower_components/fullcalendar/full-calendar.html">
<dom-module id="my-full-calendar">
    <script>
        class MyFullCalendar extends FullCalendar {
            static get is() {
                return 'my-full-calendar';
            }

            _createInitOptions() {
                var options = super._createInitOptions();
                options.eventRender = function (event, element) {
                    element.css('color', 'red');
                    return element;
                };
                return options;
            }
        }

        customElements.define(MyFullCalendar.is, MyFullCalendar);
    </script>
</dom-module>

2. Create a subclass of FullCalendar 

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import org.vaadin.stefan.fullcalendar.FullCalendar;

@Tag("my-full-calendar")
@HtmlImport("frontend://my-full-calendar.html")
public class MyFullCalendar extends FullCalendar {
    MyFullCalendar(int entryLimit) {
        super(entryLimit);
    }
}

3. Use this class in your code

calendar = new MyFullCalendar(5);

### Creating a background event
Entry entry = new Entry();
// ... setup entry details

entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
calendar.addEntry(entry);

### Adding business hours
// Single instance for "normal" business week (mo-fr)
calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK));

// Multiple instances
calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY)
        );
        
// Single instance for "each day from 9am to midnight"
calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0)));

### Using timezones
// Per default, our FC works with UTC. You can set a custom timezone to be shown for the user. 
// This will automatically update all entries on the client side.
Timezone tzBerlinGermany = new Timezone("Europe/Berlin");
calendar.setTimezone(tzBerlinGermany);

// We can also reset the timezone to default.
calendar.setTimezone(Timezone.UTC);

// We can also read the browsers timezone, after the component has been attached to the client side.
// There are other ways to obtain the browser's timezone, so you are not obliged to use the listener.
calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));

// If you want to let the calendar obtain the browser time zone automatically, you may simply use the builder.
// In that case as soon as the client connected, it will set it's timezone in the server side instance. 
FullCalendarBuilder.create().withAutoBrowserTimezone().build();

// When using timezones, entries can calculate their start and end in different ways.
entry.setStart(Instant.now()); // UTC 
entry.setStart(LocalDateTime.now(), tzBerlinGermany); // timezone is used to calculate the UTC value

entry.setCalendar(calendar); // is done automatically, when using calendar.addEntry(entry);
entry.setStart(LocalDateTime.now()); // Uses the calendars timezone (or UTC as fallback)
 
// Timezone provides some convenient methods to work with the two different temporal types
tzBerlinGermany.convertToUTC(LocalDateTime.of(2018, 10, 1, 10, 0, 0)) // Standard time, returns Instant for 9:00 UTC this day.
tzBerlinGermany.convertToUTC(LocalDateTime.of(2018, 8, 1, 10, 0, 0)) // Summer time, returns Instant for 8:00 UTC this day.
tzBerlinGermany.convertToLocalDateTime(Instant.now()) // returns a date time with +1/+2 hours (depending on summer time).

# FullCalendar Scheduler extension
This addon extends the **FullCalendar 4 web component** with the FullCalendar Scheduler, which provides 
additional resource based views (Timeline View and Vertical Resource View) for Vaadin 14+. 
For a Vaadin 10-13 version (that is built on FC 3.10.x), see https://vaadin.com/directory/component/full-calendar-scheduler-extension

It needs the basic addon (https://vaadin.com/directory/component/full-calendar-4-web-component) to work. 
Since this addon is not always updated when the basis gets an update, I suggest, that you add both dependencies 
(basis and extension) to always use the latest versions. This extension is compatible as long as the readme 
does not tells anything else.

For information about the Scheduler (functionality, features, license information, etc.) 
visit https://fullcalendar.io/scheduler. 

## License information:
Please be aware, that the FullCalender Scheduler library this addon is based on has a different license model 
then the basic FullCalendar. For details about the license, visit https://fullcalendar.io/license.

**This addon does not provide any commercial license for the Scheduler. The license model of MIT does only affect
the additional files of this addon, not the used original files.** 

## Activating the Scheduler
By default the scheduler is not active, when you use a FullCalendar instance. To have an instance with scheduler
activated, use the `withScheduler()` method of the `FullCalendarBuilder`. 

This method will throw an exception, if the scheduler extension is not on the class path.

To link a resource with entries, use the Entry subclass `ResourceEntry`. 

## Additional Features of the Scheduler extension
- Activation of the Scheduler by method in the FullCalendarBuilder.
- Adding resources to a calendar (hierarchies of resources are not yet supported). 
- Link one or multiple resources with entries.
- List of possible Scheduler based views (timeline).

*Info:* Entries are linked to calendar internally. The calendar instance is used to resolve resources by after updating an
entry on the client side.

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

## Examples
### Activating the scheduler
```
FullCalendar calendar = FullCalendarBuilder.create().withScheduler().build();

// scheduler options
((Scheduler) calendar).setSchedulerLicenseKey(...);
```

### Adding a resource to a calendar and link it with entries
```
Resource resource = new Resource(null, s, color);
calendar.addResource(resource);

// When we want to link an entry with a resource, we need to use ResourceEntry
// (a subclass of Entry)
ResourceEntry entry = new ResourceEntry(null, title, start.atStartOfDay(), start.plusDays(days).atStartOfDay(), true, true, color, "Some description...");
entry.setResource(resource);
calendar.addEntry(entry);
```

### Handling change of an entry's assigned resource by drag and drop
```
calendar.addEntryDroppedListener(event -> {
    event.applyChangesOnEntry();

    Entry entry = event.getEntry();

    if(entry instanceof ResourceEntry) {
        Set<Resource> resources = ((ResourceEntry) entry).getResources();
        if(!resources.isEmpty()) {
            // do something with the resource info
        }
    }
});
```

### Switching to a timeline view
```
calendar.changeView(SchedulerView.TIMELINE_DAY);
```

### Activate vertical resource view 
```
calendar.setGroupEntriesBy(GroupEntriesBy.RESOURCE_DATE);
```

### Using custom styles to modify FCs appearance
1. Copy the styles_scheduler.html from the github demo or create your own custom style file and place it in your applications webapp/frontend folder (e. g. webapp/frontend/styles/styles/my-custom-full-calendar-styles.html)

The github demo file can be obtained from here:
https://github.com/stefanuebe/vaadin_fullcalendar/blob/master/demo/src/main/webapp/frontend/styles_scheduler.html


2. Modify the styles as needed.

```
<custom-style>
    <style>
        html{
               --fc-timeline_fc-divider-border-style: dashed;
               --fc-timeline_fc-divider-width: 2px;
        }
    </style>
</custom-style>
```

3. Use the styles file in your application.
```
@HtmlImport("frontend://styles/full-calendar-styles-scheduler.html")
public class FullCalendarApplication extends Div {
    // ...
}
```

### Creating a subclass of FullCalendarScheduler for custom mods
1. Create a new polymer template (e.g. webapp/frontend/my-full-calendar.html):

<link rel="import" href="bower_components/fullcalendar/full-calendar-scheduler.html">
<dom-module id="my-full-calendar">
    <script>
        class MyFullCalendar extends FullCalendarScheduler {
            static get is() {
                return 'my-full-calendar';
            }

            _createInitOptions() {
                var options = super._createInitOptions();
                options.eventRender = function (event, element) {
                    element.css('color', 'red');
                    return element;
                };
                return options;
            }
        }

        customElements.define(MyFullCalendar.is, MyFullCalendar);
    </script>
</dom-module>

2. Create a subclass of FullCalendarScheduler

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

@Tag("my-full-calendar")
@HtmlImport("frontend://my-full-calendar.html")
public class MyFullCalendar extends FullCalendarScheduler {
    MyFullCalendar(int entryLimit) {
        super(entryLimit);
    }
}

3. Use this class in your code

calendar = new MyFullCalendar(5);

### Creating a resource bases background event
ResourceEntry entry = new ResourceEntry();
// ... setup entry details, including addResource()

entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
calendar.addEntry(entry);
