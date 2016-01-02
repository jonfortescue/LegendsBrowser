package legends;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.velocity.app.Velocity;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import legends.model.EntityLink;
import legends.model.HistoricalFigureLink;
import legends.model.World;
import legends.model.events.ArtFormCreatedEvent;
import legends.model.events.ChangeHfBodyStateEvent;
import legends.model.events.ChangeHfStateEvent;
import legends.model.events.EntityLawEvent;
import legends.model.events.HfConfrontedEvent;
import legends.model.events.HfDiedEvent;
import legends.model.events.HfDoesInteractionEvent;
import legends.model.events.HfRelationshipDeniedEvent;
import legends.model.events.HfSimpleBattleEvent;
import legends.model.events.SiteDisputeEvent;
import legends.xml.WorldContentHandler;
import legends.xml.handlers.XMLContentHandler;

public class Application {

	public static void main(String[] args) {
		try {
			
			World.setImage(ImageIO.read(new File("data/map.bmp")), 129, 129);
			
			
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			XMLContentHandler contentHandler = new XMLContentHandler("", xmlReader);
			WorldContentHandler worldContentHandler = new WorldContentHandler("df_world", xmlReader);
			contentHandler.registerContentHandler(worldContentHandler);
			xmlReader.setContentHandler(contentHandler);

			InputSource inputSource = new InputSource(new FileReader("data/legends.xml"));
			xmlReader.parse(inputSource);

			EntityLink.printUnknownLinkTypes();
			HistoricalFigureLink.printUnknownLinkTypes();
			
			ChangeHfStateEvent.printUnknownStates();
			HfDiedEvent.printUnknownCauses();
			HfSimpleBattleEvent.printUnknownSubtypes();
			EntityLawEvent.printUnknownLaws();
			SiteDisputeEvent.printUnknownDisputes();
			HfConfrontedEvent.printUnknownSituations();
			HfConfrontedEvent.printUnknownReasons();
			HfRelationshipDeniedEvent.printUnknownRelationships();
			HfRelationshipDeniedEvent.printUnknownReasons();
			HfDoesInteractionEvent.printUnknownInteractions();
			ChangeHfBodyStateEvent.printUnknownBodyStates();
			ArtFormCreatedEvent.printUnknownCircumstances();
			ArtFormCreatedEvent.printUnknownReasons();
			
			World.process();
			
			Properties props = new Properties();
			props.setProperty("resource.loader", "file");
			props.setProperty("file.resource.loader.path", "src/main/resources");
			props.setProperty("userdirective", "legends.helper.Decorate");

			Velocity.init(props);
			PortalServer.init();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
