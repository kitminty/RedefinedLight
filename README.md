# RedefinedLight

Lemniscate formula:

int sized = 1;
double xx = (sized*Math.sin(2*((livingEntity.tickCount+partialTicks)*0.05)))/(3-Math.cos(2*((livingEntity.tickCount+partialTicks)*0.05)));
double zz = (2*sized*Math.cos(((livingEntity.tickCount+partialTicks)*0.05)))/(3-Math.cos(2*((livingEntity.tickCount+partialTicks)*0.05)));

Todo:
1. make it so other people who have the mod will see the halo if you have it enabled 
and seperate the halo enabler so that they can only disable their own halo
2. learn about when to use arrays
3. make thing that is a square ring like [] then another rotating around and their linked because I randomly thought of it
4. check that it works if another player has the mod

May be useful later:
    public static Vec3 fromEntityCenter(Entity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ());
    }
     //Rotates {@code v} by {@code theta} radians around {@code axis}
    public static Vec3 rotate(Vec3 v, double theta, Vec3 axis) {
        if (Mth.equal(theta, 0)) {
            return v;
        }
        // Rodrigues rotation formula
        Vec3 k = axis.normalize();
        float cosTheta = Mth.cos((float) theta);
        Vec3 firstTerm = v.scale(cosTheta);
        Vec3 secondTerm = k.cross(v).scale(Mth.sin((float) theta));
        Vec3 thirdTerm = k.scale(k.dot(v) * (1 - cosTheta));
        return new Vec3(firstTerm.x + secondTerm.x + thirdTerm.x,
                firstTerm.y + secondTerm.y + thirdTerm.y,
                firstTerm.z + secondTerm.z + thirdTerm.z);
    }