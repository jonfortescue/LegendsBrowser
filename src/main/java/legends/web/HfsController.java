package legends.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import legends.helper.EventHelper;
import legends.model.HistoricalFigure;
import legends.model.World;
import legends.web.basic.Controller;
import legends.web.basic.RequestMapping;

@Controller
public class HfsController {

	@RequestMapping("/hfs")
	public Template hfs(VelocityContext context) {
		boolean leader = context.containsKey("leader");
		boolean deity = context.containsKey("deity");
		boolean force = context.containsKey("force");
		boolean vampire = context.containsKey("vampire");
		boolean werebeast = context.containsKey("werebeast");
		boolean necromancer = context.containsKey("necromancer");
		boolean alive = context.containsKey("alive");
		boolean ghost = context.containsKey("ghost");
		boolean adventurer = context.containsKey("adventurer");

		if (leader || deity || force || vampire || werebeast || necromancer || alive || ghost || adventurer) {
			context.put("elements", World.getHistoricalFigures().stream().filter(hf -> {
				if (leader && !hf.isLeader())
					return false;
				if (deity && !hf.isDeity())
					return false;
				if (force && !hf.isForce())
					return false;
				if (vampire && !hf.isVampire())
					return false;
				if (werebeast && !hf.isWerebeast())
					return false;
				if (necromancer && !hf.isNecromancer())
					return false;
				if (alive && hf.getDeathYear() != -1)
					return false;
				if (ghost && !hf.isGhost())
					return false;
				if (adventurer && !hf.isAdventurer())
					return false;
				return true;
			}).collect(Collectors.toList()));
		} else {
			context.put("elements", World.getHistoricalFigures());
		}

		return Velocity.getTemplate("hfs.vm");
	}

	@RequestMapping("/hf/{id}")
	public Template hf(VelocityContext context, int id) {
		HistoricalFigure hf = World.getHistoricalFigure(id);
		HistoricalFigure.setContext(hf);

		context.put("hf", hf);
		context.put("family", new Family(hf));
		context.put("events", World.getHistoricalEvents().stream().filter(e -> EventHelper.related(hf, e))
				.collect(Collectors.toList()));

		return Velocity.getTemplate("hf.vm");
	}

	public class FamilyMember {
		HistoricalFigure hf;
		int generation;
		int distance;
		float x;
		float offset = 0;
		boolean strongLink = false;

		FamilyMember father, mother, spouse;
		Set<FamilyMember> children = new HashSet<>();

		public FamilyMember(HistoricalFigure hf, int generation, int distance) {
			this.hf = hf;
			this.generation = generation;
			this.distance = distance;
		}

		public int getGeneration() {
			return generation;
		}

		public int getDistance() {
			return distance;
		}

		public HistoricalFigure getHf() {
			return hf;
		}

		public float getX() {
			return x;
		}

		public float getWidth() {
			if (generation == 0 && distance == 0)
				return Math.max(getWidthDown(), getWidthUp());
			else
				return getWidthDown();
		}

		public float getWidthDown() {
			return Math.max(spouse != null ? 2 : 1,
					(float) children.stream().mapToDouble(FamilyMember::getWidth).sum());

		}

		public float getWidthUp() {
			return Math.max(1, (father != null ? father.getWidthUp() : 0) + (mother != null ? mother.getWidthUp() : 0));
		}

		public void layout() {
			float diff = (getWidthUp() - getWidthDown()) / 2f;
			if (generation == 0 && distance == 0) {
				if (diff < 0)
					offset = -diff;
				layoutUp();
				if (diff > 0)
					offset = diff;
				else
					offset = 0;
			}

			float off = offset;
			for (FamilyMember c : children) {
				c.offset = off;
				c.layout();
				off += c.getWidth();
			}
			if (spouse == null) {
				x = offset + (getWidthDown() - 1) / 2;
			} else {
				x = offset - 0.5f + (getWidthDown() - 1) / 2;
				spouse.x = offset + 0.5f + (getWidthDown() - 1) / 2;
			}
		}

		public void layoutUp() {
			float off = offset;
			if (father != null) {
				father.offset = off;
				father.layoutUp();
				off += father.getWidthUp();
			}
			if (mother != null) {
				mother.offset = off;
				mother.layoutUp();
				off += mother.getWidthUp();
			}
			x = offset + (getWidthUp() - 1) / 2;
		}

		public String getRelation() {
			if (generation == 0 && distance == 0) {
				return " ";
			}
			if (generation == -3 && distance == 3) {
				// grand parents
				if (hf.isFemale())
					return "great-grandmother";
				else
					return "great-grandfather";
			}
			if (generation == -2 && distance == 2) {
				// grand parents
				if (hf.isFemale())
					return "grandmother";
				else
					return "grandfather";
			}
			if (generation == -1 && distance == 1) {
				// parents
				if (hf.isFemale())
					return "mother";
				else
					return "father";
			}
			if (generation == -1 && distance == 3) {
				// parents
				if (hf.isFemale())
					return "aunt";
				else
					return "uncle";
			}
			if (generation == 0 && distance == 1) {
				// spouse
				if (hf.isFemale())
					return "wife";
				else
					return "husband";
			}
			if (generation == 0 && distance == 2) {
				// sibling
				if (hf.isFemale())
					return "sister";
				else
					return "brother";
			}
			if (generation == 0 && distance == 3) {
				if (hf.isFemale())
					return "sister-in-law";
				else
					return "brother-in-law";
			}
			if (generation == 0 && distance == 4) {
				// spouse
				return "cousin";
			}
			if (generation == 1 && distance == 1) {
				// child
				if (hf.isFemale())
					return "daughter";
				else
					return "son";
			}
			if (generation == 1 && distance == 2) {
				// child
				if (hf.isFemale())
					return "daughter-in-law";
				else
					return "son-in-law";
			}
			if (generation == 1 && distance == 3) {
				// child
				if (hf.isFemale())
					return "niece";
				else
					return "nephew";
			}
			if (generation == 2 && distance == 2) {
				// grand child
				if (hf.isFemale())
					return "granddaughter";
				else
					return "grandson";
			}
			if (generation == 3 && distance == 3) {
				// grand child
				if (hf.isFemale())
					return "great-granddaughter";
				else
					return "great-grandson";
			}

			return "";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FamilyMember)
				return hf.getId() == ((FamilyMember) obj).hf.getId();
			return false;
		}

		@Override
		public int hashCode() {
			return hf.getId();
		}
	}

	public class FamilyLink {
		String type;
		FamilyMember m1, m2;
		int x1, x2;

		public FamilyLink(String type, FamilyMember m1, FamilyMember m2) {
			this.type = type;
			if (m1.generation <= m2.generation) {
				this.m1 = m1;
				this.m2 = m2;
			} else {
				this.m1 = m1;
				this.m2 = m1;
			}
		}

		public String getType() {
			return type;
		}

		public FamilyMember getM1() {
			return m1;
		}

		public FamilyMember getM2() {
			return m2;
		}

		public float getX1() {
			if (m1.generation != m2.generation && m1.spouse != null) {
				return (m1.x + m1.spouse.x) / 2f;
			} else
				return m1.x;
		}

		public int getX2() {
			return x2;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FamilyLink) {
				FamilyLink l = (FamilyLink) obj;
				return (l.m1.equals(m1) && l.m2.equals(m2)) || (l.m1.equals(m2) && l.m2.equals(m1));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Math.max(m1.hashCode(), m2.hashCode()) << 16 + Math.min(m1.hashCode(), m2.hashCode());
		}
	}

	public class Family {
		private List<FamilyMember> members = new ArrayList<>();
		private Set<FamilyLink> links = new LinkedHashSet<>();
		private FamilyMember root;

		public Family(HistoricalFigure hf) {
			FamilyMember m = new FamilyMember(hf, 0, 0);
			root = m;
			m.strongLink = true;

			members.add(m);
			analyzeFamily();

			root.layout();
		}

		public void addMember(FamilyMember m) {
			if (members.contains(m))
				return;
			members.add(m);
		}

		public void addMember(FamilyMember m, FamilyMember after) {
			if (members.contains(m))
				return;
			members.add(members.indexOf(after), m);
		}

	
		private void analyzeParents(FamilyMember m) {
			System.out.println(m.generation + " " + m.distance);
			HistoricalFigure father = m.hf.getHfLink("father");
			FamilyMember m1 = null, m2 = null;
			if (father.getId() != -1) {
				try {
					m1 = get(father.getId(), new FamilyMember(father, m.generation - 1, m.distance + 1));
					if (!m1.getRelation().equals("")) {
						m.father = m1;
						analyzeParents(m1);
						addMember(m1);
					} else {
						m1 = null;
					}
				} catch (MemeberExistsException e) {
					System.out.println(m.hf.getName() + ": " + e.getMessage());
				}
			}
			HistoricalFigure mother = m.hf.getHfLink("mother");
			if (mother.getId() != -1) {
				try {
					m2 = get(mother.getId(), new FamilyMember(mother, m.generation - 1, m.distance + 1));
					if (!m2.getRelation().equals("")) {
						m.mother = m2;
						analyzeParents(m2);
						addMember(m2);
					} else {
						m1 = null;
					}
				} catch (MemeberExistsException e) {
					System.out.println(m.hf.getName() + ": " + e.getMessage());
				}
			}
			if (m1 != null && m2 != null) {
				m1.spouse = m2;
				m2.spouse = m1;
				links.add(new FamilyLink("spouse", m1, m2));
				links.add(new FamilyLink("child", m1, m));
				m1.children.add(m);
				m2.children.add(m);
			} else if (m1 != null || m2 != null) {
				System.err.println("only one parent");
			}
		}

		private void analyzeSpouse(FamilyMember m) {
			HistoricalFigure spouse = m.hf.getHfLink("spouse");
			if (spouse.getId() != -1) {
				FamilyMember m2;
				try {
					m2 = get(spouse.getId(), new FamilyMember(spouse, m.generation, m.distance + 1));
					if (!m2.getRelation().equals("")) {
						m.spouse = m2;
						m2.spouse = m;
						links.add(new FamilyLink("spouse", m, m2));
						addMember(m2, m);
					}
				} catch (MemeberExistsException e) {
					e.printStackTrace();
				}
			}

		}

		private void analyzeChildren(FamilyMember m) {
			List<HistoricalFigure> children = m.hf.getHfLinks("child");
			for (HistoricalFigure c : children) {
				FamilyMember m3;
				try {
					m3 = get(c.getId(), new FamilyMember(c, m.generation + 1, m.distance + 1));
					if (!m3.getRelation().equals("")) {
						m.children.add(m3);
						links.add(new FamilyLink("child", m, m3));
						m3.strongLink = true;
						addMember(m3);
						analyzeSpouse(m3);
						analyzeChildren(m3);
					}
				} catch (MemeberExistsException e) {

				}
			}
		}

		private void analyzeFamily() {
			analyzeParents(root);
			analyzeSpouse(root);
			analyzeChildren(root);
		}

		@SuppressWarnings("serial")
		public class MemeberExistsException extends Exception {
			FamilyMember m, m2;

			public MemeberExistsException(FamilyMember m, FamilyMember m2) {
				this.m = m;
				this.m2 = m2;
			}

			@Override
			public String getMessage() {
				return m.hf.getName() + "(" + m.generation + " " + m.distance + ") = " + m2.hf.getName() + "("
						+ m2.generation + " " + m2.distance + ")";
			}

		}

		private FamilyMember get(int id, FamilyMember defaultMember) throws MemeberExistsException {
			for (FamilyMember m : members) {
				if (m != null && m.getHf().getId() == id)
					throw new MemeberExistsException(m, defaultMember);
			}
			return defaultMember;
		}

		public List<FamilyMember> getChildren(FamilyMember m) {
			return m.children.stream().filter(members::contains).collect(Collectors.toList());
		}

		public List<Integer> getGenerations() {
			return members.stream().map(FamilyMember::getGeneration).distinct().sorted().collect(Collectors.toList());
		}

		public List<FamilyMember> getMembers(int generation) {
			return members.stream().filter(m -> m.getGeneration() == generation).collect(Collectors.toList());
		}

		public Set<FamilyLink> getLinks() {
			return links;
		}

		public Map<FamilyMember, List<FamilyLink>> getGroupedLinks() {
			return links.stream().collect(Collectors.groupingBy(FamilyLink::getM1));
		}

		public float getMaxX() {
			return (float) members.stream().mapToDouble(FamilyMember::getX).max().orElse(0);
		}
	}
}
