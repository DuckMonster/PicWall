package com.emilstrom.picwall.helper;

/**
 * Created by Emil on 2014-02-20.
 */
public class Vertex2 {
	public float x, y;
	public Vertex2() {
		x = 0;
		y = 0;
	}
	public Vertex2(float xx, float yy) {
		x = xx;
		y = yy;
	}
	public Vertex2(Vertex2 v) {
		x = v.x;
		y = v.y;
	}

	public void add(Vertex2 v) {
		x += v.x;
		y += v.y;
	}
	public void subtract(Vertex2 v) {
		x -= v.x;
		y -= v.y;
	}
	public void multiply(Vertex2 v) {
		x *= v.x;
		y *= v.y;
	}
	public void multiply(float d) {
		x *= d;
		y *= d;
	}

	public Vertex2 plus(Vertex2 v) {
		return Vertex2.add(this, v);
	}
	public Vertex2 minus(Vertex2 v) {
		return Vertex2.subtract(this, v);
	}
	public Vertex2 times(Vertex2 v) {
		return Vertex2.multiply(this, v);
	}
	public Vertex2 times(float d) {
		return Vertex2.multiply(this, d);
	}

	public void normalize() {
		float l = getLength();
		x /= l;
		y /= l;
	}
	public void reverse() { multiply(-1); }

	public void copy(Vertex2 v) { x = v.x; y = v.y; }

	public float getLength() { return getLength(this); }
	public float getDirection() { return getDirection(this); }
	public Vertex2 getNormalized() {
		Vertex2 v = new Vertex2(this);
		v.normalize();

		return v;
	}

	public boolean compare(Vertex2 v) { return (x == v.x && y == v.y); }

	public Vertex3 toVertex3() {
		return new Vertex3(x, y, 0f);
	}

	////

	public static Vertex2 add(Vertex2 a, Vertex2 b) {
		return new Vertex2(a.x + b.x, a.y + b.y);
	}

	public static Vertex2 subtract(Vertex2 a, Vertex2 b) {
		return new Vertex2(a.x - b.x, a.y - b.y);
	}

	public static Vertex2 multiply(Vertex2 a, Vertex2 b) {
		return new Vertex2(a.x * b.x, a.x * b.y);
	}

	public static Vertex2 multiply(Vertex2 a, float d) {
		return new Vertex2(a.x * d, a.y * d);
	}

	public static Vertex2 normalize(Vertex2 v) {
		if (v.x == 0 && v.y == 0) return new Vertex2(0,0);

		float l = getLength(v);
		return new Vertex2(v.x / l, v.y / l);
	}

	public static Vertex2 getDirectionVertex(Vertex2 a, Vertex2 b) {
		return normalize(subtract(b, a));
	}
	public static Vertex2 getDirectionVertex(float direction) {
		return new Vertex2(-(float)Math.sin(direction / 180f * Math.PI), (float)Math.cos(direction / 180f * Math.PI));
	}

	public static float getLength(Vertex2 a, Vertex2 b) {
		Vertex2 v = subtract(a, b);
		if (v.x == 0f && v.y == 0f) return 0f;

		return v.getLength();
	}

	public static float getDirection(Vertex2 a, Vertex2 b) {
		return (float)GameMath.getDirection(a.x, a.y, b.x, b.y);
	}

	public static float getLength(Vertex2 v) {
		return (float)Math.sqrt(v.x*v.x + v.y*v.y);
	}

	public static float getDirection(Vertex2 v) {
		return (float)GameMath.getDirection(v.x, v.y, 0, 0);
	}
}