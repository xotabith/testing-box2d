package pl.mg6.testing.box2d;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class PhysicsWorld {

	private List<Body> bodies = new ArrayList<Body>();

	private World world;

	public void create(Vec2 gravity) {

		// Step 2: Create Physics World with Gravity
		boolean doSleep = false;
		world = new World(gravity, doSleep);

		// Step 3: Create Ground Box
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(new Vec2(5.0f, -2.0f));
		Body groundBody = world.createBody(groundBodyDef);
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(7.0f, 2.0f);
		
		groundBody.createFixture(polygonShape, 1.0f);
		
		groundBodyDef.position.set(new Vec2(5.0f, 32.0f));
		groundBody = world.createBody(groundBodyDef);
		
		groundBody.createFixture(polygonShape, 1.0f);
		
		polygonShape.setAsBox(2.0f, 18.0f);
		
		groundBodyDef.position.set(new Vec2(-2.0f, 16.0f));
		
		groundBody = world.createBody(groundBodyDef);
		groundBody.createFixture(polygonShape, 1.0f);
		
		groundBodyDef.position.set(new Vec2(12.0f, 16.0f));
		
		groundBody = world.createBody(groundBodyDef);
		groundBody.createFixture(polygonShape, 1.0f);
	}
	
	public void addBall(float x, float y, Object data, float density, float radius, float bounce, float friction) {
		// Create Shape with Properties
		CircleShape circleShape = new CircleShape();
		circleShape.m_radius = radius;
		
		addItem(x, y, circleShape, bounce, data, density, friction);
	}
	
	public void addBox(float x, float y, Object data, float density, float bounce, float friction) {
		float s = 0.5f;
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(s, s, new Vec2(s, s), 0.0f);
		
		addItem(x, y, polygonShape, bounce, data, density, friction);
	}

	private void addItem(float x, float y, Shape shape, float bounce, Object data, float density, float friction) {
		// Create Dynamic Body
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.userData = data;
		bodyDef.type = BodyType.DYNAMIC;
		Body body = world.createBody(bodyDef);
		bodies.add(body);



		// Assign shape to Body
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = bounce;
		body.createFixture(fixtureDef);
	}

	public void update() {
		// Update Physics World
		world.step(1.0f / 60.0f, 10, 10);

		// Print info of latest body
		//if (bodies.size() > 0) {
			//Body body = bodies.get(bodies.size() - 1);
			//Vec2 position = body.getPosition();
			//float angle = body.getAngle();
			//Log.v("Physics Test", "Pos: (" + position.x + ", " + position.y + "), Angle: " + angle);
		//}
	}

	public World getWorld() {
		return world;
	}
}