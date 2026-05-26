from typing import Any, Dict
from utils.state_utils import get_age_group


def empty_story_state() -> Dict[str, Any]:
    return {
        "character": {
            "name": "",
            "unique_trait": "",
            "likes": "",
            "fear_or_weakness": "",
            "emotion_expression": "",
        },
        "setting": {
            "place": "",
            "sensory_detail": "",
            "visual_anchor": "",
        },
        "culture_element": {
            "object_or_place": "",
            "appearance": "",
            "emotional_link": "",
        },
        "plot": {
            "incident": "",
            "first_reaction": "",
            "motivation": "",
        },
        "conflict": {
            "obstacle": "",
            "obstacle_reason": "",
        },
        "resolution": {
            "choice": "",
            "change": "",
        },
        "parent_intent": {
            "value": "",
            "desired_behavior": "",
            "avoidance": "",
            "avoid_direct_message": True,
        },
    }


def make_run_tags(
    target_age: int,
    parent_lang_code: str,
    story_lang_code: str = "ko",
    creativity_level: str = "medium",
    generate_images: bool = True,
    image_style: str = "warm watercolor children picture book illustration",
    no_moral: bool = False,
) -> Dict[str, Any]:
    return {
        "targetAge": target_age,
        "ageGroup": get_age_group(target_age),
        "parentLangCode": parent_lang_code,
        "storyLangCode": story_lang_code,
        "creativityLevel": creativity_level,
        "generateImages": generate_images,
        "imageStyle": image_style,
        "noMoral": no_moral,
    }
