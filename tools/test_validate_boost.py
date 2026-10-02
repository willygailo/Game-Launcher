#!/usr/bin/env python3
"""
tools/test_validate_boost.py — Unit tests for validate_boost.py parsing functions.
Device-free tests: runs without requiring ADB or a physical connected device.
"""

import unittest
from tools.validate_boost import (
    parse_hz_output,
    parse_cpu_governors,
    parse_game_mode_fps,
)


class TestValidateBoostParsers(unittest.TestCase):

    def test_parse_hz_output_valid(self):
        self.assertEqual(parse_hz_output("120.0", "null"), 120.0)
        self.assertEqual(parse_hz_output("null", "165.0"), 165.0)
        self.assertEqual(parse_hz_output("185", "185"), 185.0)

    def test_parse_hz_output_null_or_empty(self):
        self.assertIsNone(parse_hz_output(None, "null"))
        self.assertIsNone(parse_hz_output("", ""))
        self.assertIsNone(parse_hz_output("invalid", "not_a_number"))

    def test_parse_cpu_governors(self):
        shell_out = "performance\nperformance\npowersave\n"
        govs = parse_cpu_governors(shell_out)
        self.assertEqual(govs, ["performance", "performance", "powersave"])

        empty_out = "\n   \n\n"
        self.assertEqual(parse_cpu_governors(empty_out), [])

    def test_parse_game_mode_fps(self):
        dumpsys_1 = "GameModeSettings{package=com.mobile.legends, fps=120, mode=2}"
        self.assertEqual(parse_game_mode_fps(dumpsys_1, "com.mobile.legends"), 120)

        dumpsys_2 = "Game mode for com.tencent.ig: mode=1, mFps=185"
        self.assertEqual(parse_game_mode_fps(dumpsys_2, "com.tencent.ig"), 185)

        dumpsys_empty = "No game mode active"
        self.assertIsNone(parse_game_mode_fps(dumpsys_empty, "com.game"))


if __name__ == "__main__":
    unittest.main()
