#!/usr/bin/env python3
"""Device-free unit tests for validate_boost.py pure parsers (stdlib unittest)."""

import unittest

from validate_boost import (
    TIER_FLAGS,
    frame_durations_ms,
    parse_governor_map,
    parse_gpu_nodes,
    parse_hz,
    parse_overlay_fps,
    percentile,
)


class ParseHzTest(unittest.TestCase):
    def test_valid_values(self):
        self.assertEqual(parse_hz("165"), 165)
        self.assertEqual(parse_hz("165.0\n"), 165)
        self.assertEqual(parse_hz("60"), 60)
        self.assertEqual(parse_hz(" 90.0 "), 90)

    def test_unset_and_invalid(self):
        self.assertIsNone(parse_hz(None))
        self.assertIsNone(parse_hz(""))
        self.assertIsNone(parse_hz("null"))
        self.assertIsNone(parse_hz("undefined"))
        self.assertIsNone(parse_hz("0"))
        self.assertIsNone(parse_hz("-120"))
        self.assertIsNone(parse_hz("abc"))


class GovernorMapTest(unittest.TestCase):
    def test_parses_policy_lines(self):
        got = parse_governor_map("policy0=performance\npolicy4=performance\npolicy7=schedutil\n")
        self.assertEqual(got, {
            "policy0": "performance",
            "policy4": "performance",
            "policy7": "schedutil",
        })

    def test_ignores_garbage(self):
        self.assertEqual(parse_governor_map("error: denied\n=policy\npolicy0=\n"), {})
        self.assertEqual(parse_governor_map(None), {})


class GpuNodesTest(unittest.TestCase):
    def test_parses_node_lines(self):
        nodes = parse_gpu_nodes(
            "1c00000.qcom,kgsl-3d0=performance,807000000,807000000\n"
            "13000000.mali=simple_ondemand,200000000,850000000\n"
        )
        self.assertEqual(len(nodes), 2)
        self.assertEqual(nodes[0]["name"], "1c00000.qcom,kgsl-3d0")
        self.assertEqual(nodes[0]["gov"], "performance")
        self.assertEqual(nodes[0]["cur"], "807000000")
        self.assertEqual(nodes[0]["max"], "807000000")
        self.assertEqual(nodes[1]["gov"], "simple_ondemand")

    def test_skips_malformed(self):
        self.assertEqual(parse_gpu_nodes("garbage"), [])
        self.assertEqual(parse_gpu_nodes("name=gov,cur"), [])
        self.assertEqual(parse_gpu_nodes(None), [])


class FrameStatsTest(unittest.TestCase):
    SAMPLE = (
        "Stats since: 0\n"
        "---PROFILEDATA---\n"
        "Flags,IntendedVsync,Vsync,FrameCompleted,DrawPresentComplete,SyncQueued,"
        "SyncStart,IssueStart,FrameCompleted,SwapBuffers\n"
        "0,1000000000,1000000000,1004000000,1003500000,1000100000,1000200000,"
        "1001000000,1004000000,1004200000\n"
        "0,1016666667,1016666667,1024966667,1024000000,1016700000,1016800000,"
        "1017500000,1024966667,1025100000\n"
        "0,1033333334,1033333334,1035333334,1035000000,1033400000,1033500000,"
        "1034000000,1035333334,1035500000\n"
        "---PROFILEDATA---\n"
        "View hierarchy:\n"
    )

    def test_parses_frame_durations(self):
        durations = frame_durations_ms(self.SAMPLE)
        self.assertEqual(len(durations), 3)
        self.assertAlmostEqual(durations[0], 4.0)  # 4,000,000 ns -> 4ms
        self.assertAlmostEqual(durations[1], 8.3, places=1)
        self.assertAlmostEqual(durations[2], 2.0)

    def test_empty_and_malformed_input(self):
        self.assertEqual(frame_durations_ms(None), [])
        self.assertEqual(frame_durations_ms(""), [])
        self.assertEqual(frame_durations_ms("no tables here"), [])

    def test_percentile(self):
        values = list(range(100))  # sorted 0..99
        # nearest-rank: p99 -> 99% of values (0..98) are <= result
        self.assertEqual(percentile(values, 99), 98)
        self.assertEqual(percentile(values, 50), 50)
        self.assertIsNone(percentile([], 99))


class OverlayFpsTest(unittest.TestCase):
    def test_parses_matching_modes(self):
        self.assertEqual(
            parse_overlay_fps(
                "mode=2,useAngle=false,fps=120,downscaleFactor=1.0:"
                "mode=3,useAngle=false,fps=120,downscaleFactor=1.0"
            ),
            120,
        )
        self.assertEqual(parse_overlay_fps("mode=2,fps=90:mode=3,fps=90"), 90)
        self.assertEqual(parse_overlay_fps("fps=165"), 165)

    def test_rejects_unset_empty_or_disagreeing(self):
        self.assertIsNone(parse_overlay_fps(None))
        self.assertIsNone(parse_overlay_fps(""))
        self.assertIsNone(parse_overlay_fps("null"))
        self.assertIsNone(parse_overlay_fps("mode=2,downscaleFactor=1.0"))
        self.assertIsNone(parse_overlay_fps("mode=2,fps=120:mode=3,fps=60"))
        self.assertIsNone(parse_overlay_fps("mode=2,fps=abc"))


class TierFlagsTest(unittest.TestCase):
    def test_tier_matrix(self):
        self.assertEqual(TIER_FLAGS["flagship"]["required"], "--enable-webgpu")
        self.assertIsNone(TIER_FLAGS["flagship"]["forbidden"])
        self.assertEqual(TIER_FLAGS["mid"]["required"], "--enable-drdc")
        self.assertEqual(TIER_FLAGS["mid"]["forbidden"], "--enable-webgpu")
        self.assertEqual(TIER_FLAGS["budget"]["required"], "--enable-gpu-rasterization")
        self.assertEqual(TIER_FLAGS["budget"]["forbidden"], "--enable-webgpu")


if __name__ == "__main__":
    unittest.main(verbosity=2)
