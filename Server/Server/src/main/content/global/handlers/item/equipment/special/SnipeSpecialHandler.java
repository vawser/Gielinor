package content.global.handlers.item.equipment.special;

import core.game.node.entity.Entity;
import core.game.node.entity.combat.BattleState;
import core.game.node.entity.combat.CombatStyle;
import core.game.node.entity.impl.Animator.Priority;
import core.game.node.entity.impl.Projectile;
import core.game.node.entity.player.Player;
import core.game.node.entity.skill.Skills;
import core.game.world.update.flag.context.Animation;
import core.plugin.Initializable;
import core.plugin.Plugin;
import core.tools.RandomFunction;
import core.game.node.entity.combat.RangeSwingHandler;
import org.rs09.consts.Sounds;

import static core.api.ContentAPIKt.playGlobalAudio;

/**
 * Represents the Dorgeshuun crossbow's special attack - snipe.
 * @author Splinter
 * @version 1.0
 */
@Initializable
public final class SnipeSpecialHandler extends RangeSwingHandler implements Plugin<Object> {

	/**
	 * The special energy required.
	 */
	private static final int SPECIAL_ENERGY = 75;

	/**
	 * The attack animation. TODO: This is techically the wrong animation. The
	 * real one is incredibly similiar but I can't find it anywhere.
	 */
	private static final Animation ANIMATION = new Animation(4230, Priority.HIGH);

	@Override
	public Object fireEvent(String identifier, Object... args) {
		return null;
	}

	@Override
	public Plugin<Object> newInstance(Object arg) throws Throwable {
		CombatStyle.RANGE.getSwingHandler().register(8880, this);
		return this;
	}

	@Override
	public int swing(Entity entity, Entity victim, BattleState state) {
		Player p = (Player) entity;
		configureRangeData(p, state);
		if (state.getWeapon() == null || !Companion.hasAmmo(entity, state)) {
			entity.getProperties().getCombatPulse().stop();
			p.getSettings().toggleSpecialBar();
			return -1;
		}
		if (!((Player) entity).getSettings().drainSpecial(SPECIAL_ENERGY)) {
			return -1;
		}
        state.setStyle(CombatStyle.RANGE);
		int hit = 0;
		if (!victim.getProperties().getCombatPulse().isAttacking() || isAccurateImpact(entity, victim, CombatStyle.RANGE)) {
			hit = RandomFunction.random(calculateHit(entity, victim, 1.0) + 1);
			if (victim.getSkills().getStaticLevel(Skills.DEFENCE) >= victim.getSkills().getDynamicLevels()[Skills.DEFENCE])
				victim.getSkills().updateLevel(Skills.DEFENCE, -hit, 0);
		}
		Companion.useAmmo(entity, state, victim.getLocation());
		state.setEstimatedHit(hit);
		return 1;
	}

	@Override
	public void visualize(Entity entity, Entity victim, BattleState state) {
		playGlobalAudio(entity.getLocation(), Sounds.DTTD_BONE_CROSSBOW_SA_1080);
		entity.animate(ANIMATION);
		Projectile.create(entity, victim, 698, 36, 25, 35, 72).send();
	}
}
