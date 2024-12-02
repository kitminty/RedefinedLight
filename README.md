# RedefinedLight

Lemniscate formula:

int sized = 1;
double xx = (sized*Math.sin(2*((livingEntity.tickCount+partialTicks)*0.05)))/(3-Math.cos(2*((livingEntity.tickCount+partialTicks)*0.05)));
double zz = (2*sized*Math.cos(((livingEntity.tickCount+partialTicks)*0.05)))/(3-Math.cos(2*((livingEntity.tickCount+partialTicks)*0.05)));

Todo:
1. make it so other people who have the mod will see the halo if you have it enabled 
and seperate the halo enabler so that they can only disable their own halo