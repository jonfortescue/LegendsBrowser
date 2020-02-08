package legends.model;

import java.util.ArrayList;
import java.util.List;

import legends.Application;
import legends.model.basic.AbstractObject;
import legends.model.events.WrittenContentComposedEvent;
import legends.model.events.basic.Event;
import legends.model.events.basic.EventLocation;
import legends.model.events.basic.Filters;
import legends.xml.annotation.Xml;

public class WrittenContent extends AbstractObject {
	@Xml("title")
	private String title;
	@Xml("page_start")
	private int pageStart = -1;
	@Xml("page_end")
	private int pageEnd = -1;
	@Xml("type,form")
	private String type;
	@Xml(value = "reference", elementClass = Reference.class, multiple = true)
	private List<Reference> references = new ArrayList<>();
	@Xml(value = "style", elementClass = String.class, multiple = true)
	private List<String> styles = new ArrayList<>();
	@Xml("author,author_hfid")
	private int authorHfId = -1;
	@Xml("author_roll")
	private int authorRoll = -1;
	@Xml("form_id")
	private int formId = -1;

	private List<Event> events = new ArrayList<>();

	public String getTitle() {
		if (title == null || title.equals(""))
			return "untitled " + type;
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPageStart() {
		return pageStart;
	}

	public void setPageStart(int pageStart) {
		this.pageStart = pageStart;
	}

	public int getPageEnd() {
		return pageEnd;
	}

	public void setPageEnd(int pageEnd) {
		this.pageEnd = pageEnd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getAuthorHfId() {
		return authorHfId;
	}

	public void setAuthorHfId(int authorHfId) {
		this.authorHfId = authorHfId;
	}
	
	public int getAuthorRoll() {
		return authorRoll;
	}
	
	public void setAuthorRoll(int authorRoll) {
		this.authorRoll = authorRoll;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public List<String> getStyles() {
		return styles;
	}

	public String getUrl() {
		return Application.getSubUri() + "/writtencontent/" + id;
	}

	public String getLink() {
		if (id == -1)
			return "<i>UNKNOWN WRITTEN CONTENT</i>";
		return "<a href=\"" + getUrl() + "\" class=\"writtencontent\">" + getTitle() + "</a>";
	}

	public List<Event> getEvents() {
		return events;
	}

	public Site getAuthoredIn() {
		return events.stream().collect(Filters.filterEvent(WrittenContentComposedEvent.class))
				.map(WrittenContentComposedEvent::getLocation).map(EventLocation::getSiteId).map(World::getSite)
				.findFirst().orElse(World.UNKNOWN_SITE);
	}

	public String getAuthoredOn() {
		return events.stream().collect(Filters.filterEvent(WrittenContentComposedEvent.class)).map(Event::getDate)
				.findFirst().orElse("");
	}
	
	public String getQualityDescription() {
		if (type == null || authorRoll < 0)
			return "";
		
		switch (type) {
		case "autobiography":
		case "biography":
		case "comparative biography":
		case "cultural comparison":
		case "cultural history":
		case "essay":
		case "guide":
		case "manual":
		case "novel":
		case "play":
		case "short story":
			if (authorRoll < 10)
				return "Overall, the prose is amateurish at best.";
			else if (authorRoll < 20)
				return "Overall, the prose is not awful, but not very good either.";
			else if (authorRoll < 30)
				return "Overall, the prose is passable.";
			else
				return "Overall, the prose is great.";
		case "chronicle":
		case "dictionary":
		case "star chart":
			if (authorRoll < 10)
				return "Overall, this is a terrible compilation of information.";
			else if (authorRoll < 20)
				return "Overall, the information is not compiled very well.";
			else if (authorRoll < 30)
				return "Overall, this is a good source of information.";
			else
				return "Overall, this is a great source of information.";
		default:
			return "";
		}
	}

}
