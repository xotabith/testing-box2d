package pl.mg6.testing.box2d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TestView extends View {
	
	private PhysicsWorld world;
	private Bitmap beachBall, tennisBall, soccerBall;
	private Bitmap crate;
	
	private static Object beachBallId = new Object();
	private static Object tennisBallId = new Object();
	private static Object soccerBallId = new Object();
	private static Object crateId = new Object();
	
	private Map<Integer, MouseJoint> mouseJoints = new HashMap<Integer, MouseJoint>();
	
	public TestView(Context context) {
		super(context);
		setClickable(true);
	}
	
	private static Bitmap getImageFromArrayForSize(Resources res, int[] resIds, int[] sizes, int size) {
		Bitmap bmp = null;
		for (int i = 0; i < sizes.length; i++) {
			//Log.i("test", "size: " + sizes[i] + " " + resIds[i]);
			if (sizes[i] == size) {
				bmp = BitmapFactory.decodeResource(res, resIds[i]);
				break;
			}
		}
		if (bmp == null) {
			Matrix matrix = new Matrix();
			float scale = ((float) size) / sizes[0];
			matrix.postScale(scale, scale);
			bmp = BitmapFactory.decodeResource(res, resIds[0]);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}
		return bmp;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (beachBall == null) {
			Resources res = getResources();
			int[] beachBalls = { R.drawable.beach_ball_1280,
					R.drawable.beach_ball_800,
					R.drawable.beach_ball_480,
					R.drawable.beach_ball_320 };
			int[] soccerBalls = { R.drawable.soccer_ball_1280,
					R.drawable.soccer_ball_800,
					R.drawable.soccer_ball_480,
					R.drawable.soccer_ball_320 };
			int[] tennisBalls = { R.drawable.tennis_ball_1280,
					R.drawable.tennis_ball_800,
					R.drawable.tennis_ball_480,
					R.drawable.tennis_ball_320 };
			int[] crates = { R.drawable.crate_1280,
					R.drawable.crate_800,
					R.drawable.crate_480,
					R.drawable.crate_320 };
			int[] sizes = { 1280, 800, 480, 320 };
			int size = getWidth();
			beachBall = getImageFromArrayForSize(res, beachBalls, sizes, size);
			soccerBall = getImageFromArrayForSize(res, soccerBalls, sizes, size);
			tennisBall = getImageFromArrayForSize(res, tennisBalls, sizes, size);
			crate = getImageFromArrayForSize(res, crates, sizes, size);
		}
		canvas.drawColor(0xFF002020);
		canvas.translate(0, getHeight());
		canvas.scale(1.0f, -1.0f);
		float scale = getWidth() / 10.0f;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Body body = world.getWorld().getBodyList();
		while (body != null) {
			if (body.m_userData == null) {
				body = body.getNext();
				continue;
			}
			Vec2 position = body.getPosition();
			// Log.v("test", "xxxxx: " + (position == position2) + " " +
			// position.x + " " + position.y);
			Fixture fixture = body.getFixtureList();
			Shape shape = fixture.getShape();
			Bitmap bitmap;
			if (body.m_userData == crateId) {
				bitmap = crate;
			} else if (body.m_userData == beachBallId) {
				bitmap = beachBall;
			} else if (body.m_userData == tennisBallId) {
				bitmap = tennisBall;
			} else if (body.m_userData == soccerBallId) {
				bitmap = soccerBall;
			} else {
				throw new RuntimeException();
			}
			if (shape instanceof CircleShape) {
				CircleShape circleShape = (CircleShape) shape;
				paint.setColor(0xFF440000);
				// canvas.drawCircle(position.x, position.y,
				// circleShape.m_radius, paint);
				canvas.save();
				canvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale * position.y);
				canvas.drawBitmap(bitmap, scale * (position.x - circleShape.m_radius), scale * (position.y - circleShape.m_radius),
						new Paint(Paint.ANTI_ALIAS_FLAG));
				canvas.restore();
			} else if (shape instanceof PolygonShape) {
//				PolygonShape polygonShape = (PolygonShape) shape;
//				paint.setColor(0xFF004400);
//				int count = polygonShape.getVertexCount();
//				float[] pts = new float[4 * count];
//				for (int i = 0; i < count; i++) {
//					Vec2 v = polygonShape.getVertex(i);
//					pts[(4 * (count + i) - 2) % pts.length] = position.x + v.x;
//					pts[(4 * (count + i) - 1) % pts.length] = position.y + v.y;
//					pts[4 * i] = position.x + v.x;
//					pts[4 * i + 1] = position.y + v.y;
//				}
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale * position.y);
				// canvas.drawLines(pts, paint);
				canvas.drawBitmap(bitmap, scale * (position.x), scale * (position.y), new Paint(Paint.ANTI_ALIAS_FLAG));
				canvas.restore();
			}
			body = body.getNext();
		}
	}
	
	public void setModel(PhysicsWorld world) {
		this.world = world;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		final float x = event.getX(pointerIndex) * 10 / getWidth();
		final float y = (getHeight() - event.getY(pointerIndex)) * 10 / getWidth();
		final Vec2 vec = new Vec2(x, y);
		final int pointerId = event.getPointerId(pointerIndex);
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
			world.getWorld().queryAABB(new QueryCallback() {
				public boolean reportFixture(Fixture fixture) {
					// Log.e("queryAABB", "fixture: " + fixture);
					// world.getWorld().destroyBody(fixture.m_body);
					Body body = fixture.m_body;
					MouseJointDef jointDef = new MouseJointDef();
					jointDef.bodyA = body;
					jointDef.bodyB = body;
					jointDef.target.x = x;
					jointDef.target.y = y;
					jointDef.maxForce = 1000.0f * body.getMass();
					MouseJoint mouseJoint = (MouseJoint) world.getWorld().createJoint(jointDef);
					if (mouseJoints.containsKey(pointerId)) {
						Log.w("joint existing", "pointer id: " + pointerId);
						world.getWorld().destroyJoint(mouseJoints.get(pointerId));
					}
					mouseJoints.put(pointerId, mouseJoint);
					return false;
				}
			}, new AABB(vec, vec));
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			for (int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				if (mouseJoints.containsKey(id)) {
					vec.x = event.getX(i) * 10 / getWidth();
					vec.y = (getHeight() - event.getY(i)) * 10 / getWidth();
					mouseJoints.get(id).setTarget(vec);
				}
			}
		}
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
			if (mouseJoints.containsKey(pointerId)) {
				world.getWorld().destroyJoint(mouseJoints.remove(pointerId));
			} else {
				Random r = new Random();
				switch (r.nextInt(4)) {
					case 0:
						world.addBox(x, y, crateId, 1.0f, 0.2f, 0.4f);
						break;
					case 1:
						world.addBall(x, y, beachBallId, 0.03f, 0.75f, 0.8f, 0.3f);
						break;
					case 2:
						world.addBall(x, y, tennisBallId, 0.3f, 0.25f, 0.7f, 0.3f);
						break;
					case 3:
						world.addBall(x, y, soccerBallId, 0.5f, 0.5f, 0.5f, 0.4f);
						break;
				}
			}
		}
		return super.onTouchEvent(event);
	}
}
