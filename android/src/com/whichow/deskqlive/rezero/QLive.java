package com.whichow.deskqlive.rezero;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import java.util.Random;

public class QLive extends ApplicationAdapter {

    //	Texture img;
    OrthographicCamera camera;
    Viewport viewport;
    PolygonSpriteBatch batch;
    SkeletonRenderer renderer;
    TextureAtlas atlas;
    SkeletonBinary binary;
    Skeleton skeleton;
    AnimationState state;

    Array<Animation> animations;

    public void create () {

//		img = new Texture("dialog_device_waring.png");
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 800, camera);
        batch = new PolygonSpriteBatch();
        renderer = new SkeletonRenderer();
        atlas = new TextureAtlas(Gdx.files.internal("house_rem/house_rem.atlas"));
        binary = new SkeletonBinary(atlas);
        SkeletonData data = binary.readSkeletonData(Gdx.files.internal("house_rem/house_rem.skel"));
        skeleton = new Skeleton(data);
        skeleton.setPosition(280, -100);
        AnimationStateData stateData = new AnimationStateData(data); // Defines mixing (crossfading) between animations.
        stateData.setDefaultMix(0.5f);
        animations = stateData.getSkeletonData().getAnimations();
        state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        state.setTimeScale(1f); // Slow all animations down to 60% speed.

        // Queue animations on tracks 0 and 1.
        state.setAnimation(0, "idle", true);
//      state.setAnimation(1, "empty", false);
//      state.addAnimation(1, "gungrab", false, 2); // Keys in higher tracks override the pose from lower tracks.

    }

    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0);

        state.update(Gdx.graphics.getDeltaTime());
        state.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        batch.begin();
//		batch.draw(img, 0, 0);
        renderer.draw(batch, skeleton);
        batch.end();
    }

    public void resize (int width, int height) {
        camera.setToOrtho(false); // Update camera with new size.
        viewport.update(width, height);
    }

    public void dispose () {
        batch.dispose();
//		img.dispose();
        atlas.dispose();
    }

    public void changeAnimation() {
        Random rand = new Random();
        int randomNum = rand.nextInt(animations.size);
        Animation animation = animations.get(randomNum);
        String animName = animation.getName();
        if(animName == "idle" || animName.endsWith("_r") || animName.startsWith("walk")) {
            changeAnimation();
        } else {
            state.setAnimation(0, animation, false);
            state.addAnimation(0, "idle", true, animation.getDuration());
        }
    }
}
